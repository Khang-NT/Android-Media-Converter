package com.github.khangnt.mcp.worker

import android.content.Context
import com.github.khangnt.mcp.BuildConfig
import com.github.khangnt.mcp.FFMPEG_FILE
import com.github.khangnt.mcp.annotation.JobStatus.COMPLETED
import com.github.khangnt.mcp.annotation.JobStatus.FAILED
import com.github.khangnt.mcp.exception.UnhappyExitCodeException
import com.github.khangnt.mcp.job.Job
import com.github.khangnt.mcp.job.JobManager
import com.github.khangnt.mcp.util.catchAll
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

    private val lock = Any()
    private var hasError = false
    private var processCompleted = false

    override fun run() {
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

        // start copy pipe std out -> file output
        val copyOutputThread = CopierThread(
                sourceInput = Sources.from(process.inputStream),
                sourceOutput = commandResolver.sourceOutput,
                onCopied = jobManager::recordWriting,
                onError = { throwable ->
                    if (!hasError && !isInterrupted && !processCompleted) {
                        // this is root cause error
                        onError(throwable, "Error when write output: ${throwable.message}")
                        interrupt()
                    } else {
                        Timber.d(throwable, "Error from output thread")
                    }
                }
        )
        copyOutputThread.start()
        copierThreads.add(copyOutputThread)

        // start copy source input -> tcp socket output -> FFmpeg input via tcp protocol
        for (tcpInput in commandResolver.tcpInputs) {
            val socketOutput = SocketSourceOutput(tcpInput.address)
            val copierThread = CopierThread(
                    sourceInput = tcpInput.sourceInput,
                    sourceOutput = socketOutput,
                    onCopied = {},
                    onError = { throwable ->
                        if (!hasError && !isInterrupted && !processCompleted) {
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
        if (BuildConfig.DEBUG) {
            val loggerThread = LoggerThread(process.errorStream)
            loggerThread.start()
            copierThreads.add(loggerThread)
        }

        // wait process complete
        val exitCode = try {
            process.waitFor().apply { processCompleted = true }
        } catch (interruptException: InterruptedException) {
            // 1. Maybe this thread is interrupted by parent thread
            // 2. Maybe this thread is interrupted by one of copier thread
            if (!hasError) {
                // root cause -> interrupted by parent thread -> Job canceled
                onError(interruptException, "Job was canceled")
            }
            // shutdown process
            catchAll { process.destroy() }
            // shutdown copier thread as well
            interruptThreads(copierThreads, wait = false)
            return
        }

        // finally, ensure shutdown all copier thread
        interruptThreads(copierThreads, wait = false)

        if (exitCode != 0) {
            // unhappy exit code
            val ex = UnhappyExitCodeException(code = exitCode)
            onError(ex, ex.message!!)
            return
        }

        // Successful
        job = jobManager.updateJobStatus(job, COMPLETED)
        onCompleteListener(job)
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

                Timber.d(throwable, message)
                job = jobManager.updateJobStatus(job, FAILED, message)
                onErrorListener(job, throwable)
            }
        }
    }

    private class LoggerThread(val input: InputStream): Thread() {
        override fun run() {
            catchAll {
                InputStreamReader(input).use { inputReader ->
                    inputReader.forEachLine { line ->
                        Timber.d(line)
                    }
                }
            }
        }
    }
}