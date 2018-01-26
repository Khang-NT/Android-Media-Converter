package com.github.khangnt.mcp.ui.job_manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.worker.WorkingPaths
import com.github.khangnt.mcp.worker.makeWorkingPaths
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_job_logs.*
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

const val EXTRA_JOB_ID = "JobLogsActivity:jobId"

class JobLogsActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context, jobId: Long) {
            context.startActivity(Intent(context, JobLogsActivity::class.java)
                    .putExtra(EXTRA_JOB_ID, jobId))
        }
    }

    private var logFile: File? = null
    private var jobId: Long = -1
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_logs)

        jobId = intent.getLongExtra(EXTRA_JOB_ID, -1)
        check(jobId > -1, { "Invalid job ID: $jobId" })

        val workingPaths: WorkingPaths = makeWorkingPaths(applicationContext)
        logFile = workingPaths.getLogFileOfJob(jobId)

        disposable = Observable.interval(5, TimeUnit.SECONDS)
                .map { logFile!!.readText() }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ text -> tvLogs.text = text }, Timber::e)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}
