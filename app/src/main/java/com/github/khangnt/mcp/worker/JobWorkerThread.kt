package com.github.khangnt.mcp.worker

import android.content.ContentResolver
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import com.github.khangnt.mcp.FFMPEG_FILE
import com.github.khangnt.mcp.annotation.JobStatus.*
import com.github.khangnt.mcp.exception.UnhappyExitCodeException
import com.github.khangnt.mcp.job.Job
import com.github.khangnt.mcp.job.JobManager
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.closeQuietly
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by Khang NT on 1/3/18.
 * Email: khang.neon.1997@gmail.com
 */

class JobWorkerThread(
        private val appContext: Context,
        private var job: Job,
        private val jobManager: JobManager,
        private val onCompleteListener: (Job) -> Unit,
        private val onErrorListener: (Job, Throwable?) -> Unit
) : Thread() {
    companion object {
        private fun getPid(process: Process): Int? {
            return try {
                val field = process.javaClass.getDeclaredField("pid")
                field.isAccessible = true
                field?.getInt(process)
            } catch (ignore: Throwable) {
                null
            }
        }
    }

    private val lock = Any()
    private var hasError = false

    override fun run() {
        Timber.d("Start working on job: ${job.title}")

        val ffmpegPath = File(appContext.applicationInfo.nativeLibraryDir, FFMPEG_FILE)
        val commandResolver = try {
            CommandResolver.resolve(appContext, job.command, ffmpegPath)
        } catch (resolveCommandError: Throwable) {
            onError(resolveCommandError, "Resolve command failed: ${resolveCommandError.message}")
            return
        }

        val process = try {
            startProcess(commandResolver)
                    .also { Thread.sleep(500) } // delay a bit for process initializing
        } catch (securityException: SecurityException) {
            onError(securityException, "Security error when start " +
                    "FFmpeg process: ${securityException.message}")
            return
        } catch (interruptException: InterruptedException) {
            onError(interruptException, "Job was canceled")
            return
        } catch (startProcessError: Throwable) {
            onError(startProcessError, "Start FFmpeg failed: ${startProcessError.message}")
            return
        }

        // start all copier thread
        val copierThreads = mutableListOf<Thread>()

        // start copy source input -> tcp socket output -> FFmpeg input via tcp protocol
        for (tcpInput in commandResolver.tcpInputs) {
            val socketOutput = SocketSourceOutput(tcpInput.address)
            val copierThread = CopierThread(
                    sourceInput = tcpInput.sourceInput,
                    sourceOutput = socketOutput,
                    onError = { throwable ->
                        if (!hasError && !isInterrupted) {
                            // this is root cause
                            onError(throwable, "Error when handle input: ${throwable.message}")
                            interrupt()
                        } else {
                            Timber.d(throwable, "Error from input thread: ${tcpInput.address}")
                        }
                    }
            )
            copierThread.start()
            copierThreads.add(copierThread)
        }

        // start logger thread
        val loggerThread = LoggerThread(process.errorStream, jobManager)
        loggerThread.start()
        copierThreads.add(loggerThread)

        // wait process complete
        val exitCode = try {
            process.waitFor()
        } catch (interruptException: InterruptedException) {
            // 1. Maybe this thread is interrupted by parent thread
            // 2. Maybe this thread is interrupted by one of copier thread
            if (!hasError) {
                // root cause -> interrupted by parent thread -> Job canceled
                onError(interruptException, "Job was canceled")
            }
            // shutdown process
            catchAll {
                process.inputStream.closeQuietly()
                process.outputStream.closeQuietly()
                process.errorStream.closeQuietly()
                getPid(process)?.let { pid ->
                    Timber.d("Kill pid: $pid")
                    android.os.Process.killProcess(pid)
                }
                process.destroy()
            }
            // shutdown copier thread as well
            interruptThreads(copierThreads, wait = false)

            // delete temp file as well
            commandResolver.tempFile.delete()
            return
        }

        // finally, ensure shutdown all copier thread
        interruptThreads(copierThreads, wait = false)

        if (exitCode != 0) {
            // unhappy exit code
            val ex = UnhappyExitCodeException(code = exitCode)
            onError(ex, ex.message!!)

            // delete temp file as well
            commandResolver.tempFile.delete()
            return
        }

        // Convert successful, now copy temp output to real target output
        job = jobManager.updateJobStatus(job, RUNNING, "Writing converted file to target")
        val copyOutputRunnable = CopierThread(
                sourceInput = commandResolver.tempFileSourceInput,
                sourceOutput = commandResolver.sourceOutput,
                onError = {
                    onError(it, "Write to output file failed: ${it.message}. Please check output path.")
                    commandResolver.tempFile.delete()
                },
                onSuccess = {
                    job = jobManager.updateJobStatus(job, COMPLETED)
                    onCompleteListener(job)
                    with(Uri.parse(commandResolver.command.output)) {
                        if (scheme == ContentResolver.SCHEME_FILE) {
                            val filePath = path
                            MediaScannerConnection.scanFile(appContext, arrayOf(filePath),
                                    null, null)
                        }
                    }
                    commandResolver.tempFile.delete()
                }
        )
        copyOutputRunnable.run() // not start(), avoid spawn new thread
    }

    private fun startProcess(commandResolver: CommandResolver): Process {
        val cmdArray = listOf(
                "sh", "-c",
                commandResolver.execCommand
        )
        return ProcessBuilder()
                .apply { environment().putAll(commandResolver.command.environmentVars) }
                .command(cmdArray)
                .start()
    }


    private fun interruptThreads(threads: List<Thread>, wait: Boolean = false) {
        threads.forEach {
            it.interrupt()
            if (wait && !interrupted()) {
                // catch interrupt exception if happen
                catchAll(printLog = true) { it.join() }
            }
        }
    }

    private fun onError(throwable: Throwable, message: String) {
        synchronized(lock) {
            if (!hasError) {
                hasError = true

                Timber.d(throwable, "%s", message)
                job = jobManager.updateJobStatus(job, FAILED, message)
                onErrorListener(job, throwable)
            }
        }
    }

    private class LoggerThread(val input: InputStream, val jobManager: JobManager) : Thread() {
        private val regex = Regex("(\\w+=\\s*([^\\s]+))")

        init {
            jobManager.recordOutputSize("")
        }

        override fun run() {
            catchAll {
                InputStreamReader(input).use { inputReader ->
                    inputReader.forEachLine { line ->
                        var size: String? = null
                        var bitrate: String? = null
                        regex.findAll(line).forEach { matchResult ->
                            if (matchResult.groupValues[1].startsWith("size=")) {
                                size = matchResult.groupValues[2]
                            } else if (matchResult.groupValues[1].startsWith("bitrate=")) {
                                bitrate = matchResult.groupValues[2]
                            }
                        }
                        val stringBuilder = StringBuilder()
                        if (size !== null) {
                            stringBuilder.append(size)
                        }
                        if (bitrate !== null) {
                            stringBuilder.append(" br=").append(bitrate)
                        }
                        if (!stringBuilder.isBlank()) {
                            jobManager.recordOutputSize(stringBuilder.toString())
                        }
                        Timber.d(line)
                    }
                }
            }
            jobManager.recordOutputSize("")
        }
    }

}