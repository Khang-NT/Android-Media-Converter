package com.github.khangnt.mcp.worker

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.annotation.JobStatus.RUNNING
import com.github.khangnt.mcp.exception.UnhappyExitCodeException
import com.github.khangnt.mcp.getKnownReasonOf
import com.github.khangnt.mcp.job.Job
import com.github.khangnt.mcp.job.JobManager
import com.github.khangnt.mcp.reportNonFatal
import com.github.khangnt.mcp.util.UriUtils
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.closeQuietly
import com.github.khangnt.mcp.util.deleteRecursiveIgnoreError
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

/**
 * Created by Khang NT on 1/3/18.
 * Email: khang.neon.1997@gmail.com
 */

class JobWorkerThread(
        private val appContext: Context,
        private var job: Job,
        private val jobManager: JobManager,
        private val onCompleteListener: (Job) -> Unit,
        private val onErrorListener: (Job, Throwable?) -> Unit,
        private val workingPaths: WorkingPaths = makeWorkingPaths(appContext)
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
    private var jobTempDir: File? = null

    override fun run() {
        Timber.d("Start working on job: ${job.title}")

        jobTempDir = try {
            workingPaths.getTempDirForJob(job.id)
        } catch (error: Throwable) {
            onError(error, "Error: ${error.message}")
            return
        }

        val commandResolver = try {
            CommandResolver.resolve(appContext, jobTempDir!!, workingPaths.ffmpegPath, job.command)
        } catch (resolveCommandError: Throwable) {
            onError(resolveCommandError, "Init failed: ${resolveCommandError.message}")
            return
        }

        val startTime = System.currentTimeMillis()

        var process: Process? = null
        try {
            process = startProcess(commandResolver)
        } catch (securityException: SecurityException) {
            onError(securityException, "Security error when start " +
                    "FFmpeg process: ${securityException.message}")
        } catch (interruptException: InterruptedException) {
            onError(interruptException, "Job was canceled")
        } catch (startProcessError: Throwable) {
            onError(startProcessError, "Start FFmpeg failed: ${startProcessError.message}")
        }

        if (process === null) return  // error happened

        val loggerThread = LoggerThread(process.errorStream, jobManager)
        loggerThread.start()

        // wait process complete
        val exitCode = try {
            process.waitFor()
        } catch (interruptException: InterruptedException) {
            onError(interruptException, "Job was canceled")
            // shutdown process
            catchAll {
                process.inputStream.closeQuietly()
                process.outputStream.closeQuietly()
                getPid(process)?.let { pid ->
                    Timber.d("Kill pid: $pid")
                    android.os.Process.killProcess(pid)
                }
                process.destroy()
            }

            catchAll {
                loggerThread.interrupt()
                loggerThread.join()
            }
            return
        }

        catchAll {
            // wait logger thread complete
            loggerThread.join()
        }

        if (exitCode != 0) {
            // unhappy exit code
            val ex = UnhappyExitCodeException(code = exitCode, log = loggerThread.lastLine)
            onError(ex, ex.message!!)
            return
        }

        // Convert successful, now copy temp output to real target output
        job = jobManager.updateJobStatus(job, RUNNING, "Convert success, copying output...")
        var tempIs: InputStream? = null
        var destOs: OutputStream? = null
        try {
            tempIs = commandResolver.tempFileSourceInput.openInputStream()
            destOs = commandResolver.sourceOutput.openOutputStream()
            tempIs.copyTo(destOs)
        } catch (error: Throwable) {
            onError(error, "Write output file failed: ${error.message}. Please check output path.")
            return
        } finally {
            tempIs.closeQuietly()
            destOs.closeQuietly()
            commandResolver.tempFileSourceInput.closeQuietly()
            commandResolver.sourceOutput.closeQuietly()
        }

        Timber.d("Conversion success, take %d ms, output: %s",
                System.currentTimeMillis() - startTime, job.command.output)

        // completed
        job = jobManager.updateJobStatus(job, JobStatus.COMPLETED)
        onCompleteListener(job)
        catchAll {
            UriUtils.getPathFromUri(appContext, Uri.parse(commandResolver.command.output))
        }?.let { filePath ->
            // notify media scanner
            MediaScannerConnection.scanFile(appContext, arrayOf(filePath),
                    null, null)
        }

        // clean up temp folder
        jobTempDir?.deleteRecursiveIgnoreError()
    }

    private fun startProcess(commandResolver: CommandResolver): Process {
        val cmdArray = listOf(
                "sh", "-c",
                commandResolver.execCommand
        )
        Timber.d("Start process with command: ${commandResolver.execCommand}")
        Timber.d("Final output: ${commandResolver.command.output}")
        return ProcessBuilder()
                .apply { environment().putAll(commandResolver.command.environmentVars) }
                .command(cmdArray)
                .start()
    }

    private fun onError(error: Throwable, message: String) {
        val errorDetail = getKnownReasonOf(error, appContext, message)
        job = jobManager.updateJobStatus(job, JobStatus.FAILED, errorDetail)
        onErrorListener(job, error)

        // clean up temp folder
        jobTempDir?.deleteRecursiveIgnoreError()

        Timber.d(error, "%s", message)
        reportNonFatal(error, "JobWorkerThread#onError", message)
    }

    private class LoggerThread(val input: InputStream, val jobManager: JobManager) : Thread() {
        private val regex = Regex("(\\w+=\\s*([^\\s]+))")
        var lastLine: String? = null

        init {
            jobManager.recordLiveLog("")
        }

        override fun run() {
            catchAll {
                InputStreamReader(input).use { inputReader ->
                    inputReader.forEachLine { line ->
                        lastLine = line
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
                            jobManager.recordLiveLog(stringBuilder.toString())
                        }
                        Timber.d(line)
                    }
                }
            }
            jobManager.recordLiveLog("")
        }
    }

}