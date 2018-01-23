package com.github.khangnt.mcp.worker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.getKnownReasonOf
import com.github.khangnt.mcp.job.Job
import com.github.khangnt.mcp.job.JobManager
import com.github.khangnt.mcp.reportNonFatal
import com.github.khangnt.mcp.util.deleteRecursiveIgnoreError
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadSampleListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.model.FileDownloadStatus
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.lang.Exception

/**
 * Created by Khang NT on 1/22/18.
 * Email: khang.neon.1997@gmail.com
 */

private const val DOWNLOAD_TASK_UPDATE_INTERVAL = 500

/**
 * Some inputs with scheme http://, https://, content:// can't be handled
 * by FFmpeg internally (only support file and pipe protocol). These inputs need to copy to temp folder
 * and will be deleted after job completed.
 *
 * JobPrepareThread was created to download/copy inputs to temp folder, where ffmpeg can read directly.
 */
class JobPrepareThread(
        private val appContext: Context,
        private var job: Job,
        private val jobManager: JobManager,
        private val onCompleteListener: (Job) -> Unit,
        private val onErrorListener: (Job, Throwable?) -> Unit,
        private val workingPaths: WorkingPaths = makeWorkingPaths(appContext)
): Thread() {
    private var jobTempDir: File? = null

    override fun run() {
        // start preparing
        Timber.d("Start preparing job: ${job.title}")

        jobTempDir = try {
            workingPaths.getTempDirForJob(job.id)
        } catch (error: Throwable) {
            onError(error, "Error: ${error.message}")
            return
        }

        val contentResolver = appContext.contentResolver

        val inputs = job.command.inputs
        inputs.forEachIndexed { index, input ->
            val inputUri = Uri.parse(input)
            when (inputUri.scheme.toLowerCase()) {
                ContentResolver.SCHEME_CONTENT -> {
                    // content:// can't be recognized by any ffmpeg protocol
                    val inputCopyTo = makeInputTempFile(jobTempDir!!, index)
                    job = jobManager.updateJobStatus(job, JobStatus.PREPARING, "Copying input $index")
                    try {
                        contentResolver.openInputStream(inputUri).use { inputStream ->
                            val outputStream = contentResolver.openOutputStream(Uri.fromFile(inputCopyTo))
                            BufferedOutputStream(outputStream).use { bufferedOutputStream ->
                                inputStream.copyTo(bufferedOutputStream)
                            }
                        }
                    } catch (anyError: Throwable) {
                        onError(anyError, "Prepare input $index failed: ${anyError.message}")
                        return
                    }
                }
                "http", "https" -> { // ffmpeg not compiled to support http/https protocol
                    val inputDownloadTo = makeInputTempFile(jobTempDir!!, index).absolutePath
                    job = jobManager.updateJobStatus(job, JobStatus.PREPARING, "Downloading input $index")
                    val downloadTask = FileDownloader.getImpl().create(input)
                            .setForceReDownload(true)
                            .setPath(inputDownloadTo)
                            .setCallbackProgressMinInterval(DOWNLOAD_TASK_UPDATE_INTERVAL)
                            .setSyncCallback(true)
                            .setListener(object: FileDownloadSampleListener() {
                                override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                                    val speed = task.speed
                                    val percent = if (totalBytes > 0) (soFarBytes * 100 / totalBytes) else -1
                                    val percentString = if (percent > 0) "$percent%" else ""
                                    val status = "Downloading input $index\n${speed}KB/s $percentString"
                                    job = jobManager.updateJobStatus(job, JobStatus.PREPARING, status)
                                }
                            })
                    downloadTask.start()

                    while (true) {
                        try {
                            Thread.sleep(DOWNLOAD_TASK_UPDATE_INTERVAL.toLong())
                        } catch (interruptedException: InterruptedException) {
                            onError(interruptedException, "Job was cancelled")
                            FileDownloader.getImpl().clear(downloadTask.id, inputDownloadTo)
                            return
                        }
                        if (FileDownloadStatus.isOver(downloadTask.status.toInt())) {
                            if (downloadTask.status == FileDownloadStatus.error) {
                                val error = downloadTask.errorCause ?: Exception("Download input $index failed")
                                onError(error, "Download input $index failed: ${error.message}")
                                return
                            } else {
                                Timber.d("Download input $index completed")
                                break
                            }
                        }
                    }
                }
            }
        }

        // prepared -> ready to convert
        job = jobManager.updateJobStatus(job, JobStatus.READY)
        onCompleteListener(job)
    }

    private fun onError(error: Throwable, message: String) {
        val errorDetail = getKnownReasonOf(error, appContext, message)
        job = jobManager.updateJobStatus(job, JobStatus.FAILED, errorDetail)
        onErrorListener(job, error)

        // clean up temp dir
        jobTempDir?.deleteRecursiveIgnoreError()

        Timber.d(error, "%s", message)
        reportNonFatal(error, "JobPrepareThread#onError", message)
    }
}