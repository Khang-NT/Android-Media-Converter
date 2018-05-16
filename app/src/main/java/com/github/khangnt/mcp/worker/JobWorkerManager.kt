package com.github.khangnt.mcp.worker

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.provider.DocumentFile
import com.github.khangnt.mcp.BuildConfig
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.db.job.JobRepository
import com.github.khangnt.mcp.ui.prefs.SharedPrefs
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.deleteRecursiveIgnoreError
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executors


const val ACTION_JOB_DONE = "${BuildConfig.APPLICATION_ID}.action.jobDone"
const val EXTRA_JOB_STATUS = "ConverterService:JobStatus"
const val EXTRA_JOB_OUTPUT = "ConverterService:JobOutput"

class JobWorkerManager(
        private val appContext: Context,
        private val jobRepository: JobRepository,
        private val sharedPreferences: SharedPrefs
) {

    private val scheduler: Scheduler
    private val workingPaths: WorkingPaths
    private var prepareThread: JobPrepareThread? = null
    private var workerThread: JobWorkerThread? = null

    init {
        val executor = Executors.newSingleThreadExecutor { Thread(it, "JobWorkerManager") }
        scheduler = Schedulers.from(executor)
        workingPaths = makeWorkingPaths(appContext)
        initialize()
    }

    private fun execute(action: () -> Unit) {
        Completable.fromAction(action).subscribeOn(scheduler).subscribe()
    }

    /**
     * Notify when job FAILED or COMPLETED
     */
    private fun onJobDone(job: Job) {
        if (job.status == JobStatus.COMPLETED) {
            sharedPreferences.successJobsCount += 1
        }

        val intent = Intent(ACTION_JOB_DONE)
        intent.putExtra(EXTRA_JOB_ID, job.id)
        intent.putExtra(EXTRA_JOB_STATUS, job.status)
        intent.putExtra(EXTRA_JOB_OUTPUT, job.command.output)
        appContext.sendBroadcast(intent)
    }

    private fun initialize() = execute {
        val previousRunning = jobRepository.getJobsByStatus(JobStatus.RUNNING).blockingGet()
        previousRunning.forEach {
            val job = it.copy(status = JobStatus.READY, statusDetail = "Ready to restart")
            jobRepository.updateJob(job, ignoreError = false).subscribe()
        }
        val previousPreparing = jobRepository.getJobsByStatus(JobStatus.PREPARING).blockingGet()
        previousPreparing.forEach {
            val job = it.copy(status = JobStatus.PENDING, statusDetail = "Ready to prepare again")
            jobRepository.updateJob(job, ignoreError = false).subscribe()
        }
        maybeLaunchWorker()
    }

    fun maybeLaunchWorker() = execute {
        if (!prepareThread.isRunning()) {
            val pendingJob = jobRepository.nextPendingJob().blockingGet().value
            if (pendingJob != null) {
                val prepareJob = pendingJob.copy(status = JobStatus.PREPARING)
                jobRepository.updateJob(prepareJob, false).subscribe()
                prepareThread = JobPrepareThread(appContext, prepareJob, jobRepository,
                        this::onJobPrepared, this::onJobFailed, workingPaths)
                prepareThread?.start()
            }
        }
        if (!workerThread.isRunning()) {
            val readyJob = jobRepository.nextReadyJob().blockingGet().value
            if (readyJob != null) {
                val runJob = readyJob.copy(status = JobStatus.RUNNING)
                jobRepository.updateJob(runJob, false).subscribe()
                workerThread = JobWorkerThread(appContext, runJob, jobRepository,
                        this::onJobCompleted, this::onJobFailed, workingPaths)
                workerThread?.start()
            }
        }

        if (prepareThread.isRunning() || workerThread.isRunning()) {
            ConverterService.startForeground(appContext)
        } else {
            ConverterService.stopForeground(appContext)
            // clean up temp files
            catchAll { workingPaths.getListJobTempDir().forEach { it.deleteRecursiveIgnoreError() } }
        }
    }

    private fun onJobPrepared(job: Job): Unit = execute {
        Timber.d("onJobPrepared: $job")
        catchAll { prepareThread?.join(3000) }
        maybeLaunchWorker()
    }

    private fun onJobCompleted(job: Job): Unit = execute {
        onJobDone(job)
        catchAll { workerThread?.join(3000) }
        maybeLaunchWorker()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onJobFailed(job: Job, throwable: Throwable?): Unit = execute {
        onJobDone(job)
        catchAll {
            // delete output file if size == 0
            val outputUri = Uri.parse(job.command.output)
            if (outputUri.scheme == ContentResolver.SCHEME_CONTENT) {
                val docFile = DocumentFile.fromSingleUri(appContext, outputUri)
                if (docFile != null && docFile.length() < 10 * 1024L) {
                    docFile.delete()
                }
            } else {
                val file = File(outputUri.path)
                if (file.length() < 10 * 1024L) {
                    file.delete()
                }
            }
        }
        catchAll { workerThread?.join(3000) }
        maybeLaunchWorker()
    }

    fun addJob(job: Job) = execute {
        with(Uri.parse(job.command.output)) {
            if (scheme == "content") {
                catchAll {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        appContext.contentResolver.takePersistableUriPermission(this,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                }
            }
        }
        jobRepository.addJob(job).subscribe { newJob ->
            Timber.d("Added new job: $newJob")
            maybeLaunchWorker()
        }
    }

    fun cancelJob(jobId: Long) = execute {
        when(jobId) {
            prepareThread?.job?.id -> prepareThread?.interrupt()
            workerThread?.job?.id -> workerThread?.interrupt()
            else -> {
                jobRepository.deleteJob(jobId, ignoreError = true).subscribe {
                    catchAll {
                        workingPaths.getTempDirForJob(jobId).deleteRecursiveIgnoreError()
                        workingPaths.getTempDirForJob(jobId).delete()
                    }
                }
            }
        }
        maybeLaunchWorker()
    }

    private fun Thread?.isRunning(): Boolean = this != null && isAlive && !isInterrupted

}