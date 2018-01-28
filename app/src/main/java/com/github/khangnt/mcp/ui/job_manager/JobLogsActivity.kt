package com.github.khangnt.mcp.ui.job_manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.worker.makeWorkingPaths
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_job_logs.*
import timber.log.Timber


private const val EXTRA_JOB_ID = "JobLogsActivity:jobId"
private const val EXTRA_JOB_TITLE = "JobLogsActivity:jobTitle"

class JobLogsActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context, jobId: Long, jobTitle: String) {
            context.startActivity(Intent(context, JobLogsActivity::class.java)
                    .putExtra(EXTRA_JOB_ID, jobId)
                    .putExtra(EXTRA_JOB_TITLE, jobTitle))
        }
    }

    private var jobId: Long = -1
    private var jobTitle: String? = null
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_logs)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        title = ""

        val jobIdExtra = intent.getLongExtra(EXTRA_JOB_ID, -1)
        val jobTitleExtra = intent.getStringExtra(EXTRA_JOB_TITLE) ?: ""
        setJobInfo(jobIdExtra, jobTitleExtra)

        swipeRefreshLayout.setOnRefreshListener(this::reload)
        tvJobTitle.setOnClickListener { switchJob() }
    }

    private fun setRefreshing(isRefreshing: Boolean) {
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = isRefreshing }
    }

    private fun setJobInfo(jobId: Long, jobTitle: String) {
        check(jobId > -1 && jobTitle.isNotEmpty(), { "Invalid job: $jobId - $jobTitle" })

        this.jobId = jobId
        this.jobTitle = jobTitle
        tvJobTitle.text = jobTitle
        reload()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun reload() {
        setRefreshing(true)

        disposable?.dispose()
        disposable = Observable
                .fromCallable {
                    val workingPaths = makeWorkingPaths(this)
                    val logFile = workingPaths.getLogFileOfJob(jobId)
                    return@fromCallable logFile.readText()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { setRefreshing(false) }
                .subscribe({ text ->
                    tvLogs.text = text
                    tvErrorMessage.visibility = View.GONE
                }, { error ->
                    Timber.d(error, "Error when load log of job $jobId")
                    tvLogs.text = null
                    tvErrorMessage.text = error.message
                    tvErrorMessage.visibility = View.VISIBLE
                })
    }

    private fun switchJob() {
        AlertDialog.Builder(this)
                .setSingleChoiceItems(arrayOf("Job 1: Hello", "Job 2: World"), 0, { _, which ->
                    Toast.makeText(this, "Selected $which", Toast.LENGTH_SHORT).show()
                })
                .setTitle("Select job")
                .setPositiveButton("OK", { _, _ ->
                    // set Job info
                })
                .setNegativeButton("Cancel", null)
                .show()
    }
}
