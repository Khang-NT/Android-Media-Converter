package com.github.khangnt.mcp.ui.job_manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.worker.makeWorkingPaths
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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
    private val compositeDisposable = CompositeDisposable()


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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_job_logs_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reload -> {
                reload()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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
        compositeDisposable.dispose()
    }

    private fun reload() {
        setRefreshing(true)

        disposable?.dispose() // cancel previous loading if it still not finish
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
                    tvLogs.clearFocus()
                    tvLogs.text = text
                    tvErrorMessage.visibility = View.GONE
                }, { error ->
                    Timber.d(error, "Error when load log of job $jobId")
                    tvLogs.text = null
                    tvErrorMessage.text = error.message
                    tvErrorMessage.visibility = View.VISIBLE
                })
        compositeDisposable.add(disposable!!)
    }

    private fun switchJob() {
        tvJobTitle.isEnabled = false // prevent use click when loading job list

        val selected = arrayOf(0)
        val disposable = SingletonInstances.getJobManager()
                .getJob(JobStatus.RUNNING, JobStatus.COMPLETED, JobStatus.FAILED)
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { tvJobTitle.isEnabled = true }
                .subscribe ({ jobList ->
                    val selectOptions = Array(jobList.size, { index -> jobList[index].title})
                    selected[0] = jobList.indexOfFirst { it.id == jobId }

                    AlertDialog.Builder(this)
                            .setSingleChoiceItems(selectOptions, selected[0], { _, which ->
                                selected[0] = which
                            })
                            .setTitle(getString(R.string.switch_job_dialog_title))
                            .setPositiveButton(R.string.action_ok, { _, _ ->
                                setJobInfo(jobList[selected[0]].id, selectOptions[selected[0]])
                            })
                            .setNegativeButton(R.string.action_cancel, null)
                            .show()
                }, { error ->
                    Timber.d(error)
                    Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                })
        compositeDisposable.add(disposable)
    }
}
