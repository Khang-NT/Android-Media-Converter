package com.github.khangnt.mcp.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.github.khangnt.mcp.ID_UNSET
import com.github.khangnt.mcp.PLAY_STORE_PACKAGE
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.SingletonInstances.Companion.getSharedPrefs
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.util.hasWriteStoragePermission
import com.github.khangnt.mcp.util.openPlayStore
import com.github.khangnt.mcp.worker.ACTION_JOB_STATUS_CHANGED
import com.github.khangnt.mcp.worker.EXTRA_JOB_ID
import com.github.khangnt.mcp.worker.EXTRA_JOB_STATUS
import timber.log.Timber
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS

class MainActivity : SingleFragmentActivity() {

    private val jobStatusChangedReceiver = JobStatusChangedReceiver()

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment = MainFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasWriteStoragePermission(this)) {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    private fun showRateDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.rate_us_title)
                .setMessage(R.string.rate_us_message)
                .setPositiveButton(R.string.rate_us_love_it, { _, _ ->
                    openPlayStore(this, PLAY_STORE_PACKAGE)
                    getSharedPrefs().isRated = true
                })
                .setNeutralButton(R.string.rate_us_not_now, { _, _ ->
                    // don't show this dialog again, until next day
                    getSharedPrefs().delayRateDialogUntil =
                            System.currentTimeMillis() + MILLISECONDS.convert(1, DAYS)
                })
                .setNegativeButton(R.string.rate_us_never, { _, _ ->
                    getSharedPrefs().isRated = true
                })
                .show()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(jobStatusChangedReceiver, IntentFilter(ACTION_JOB_STATUS_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(jobStatusChangedReceiver)
    }

    private fun onJobStatusChanged(jobId: Long, @JobStatus jobStatus: Int) {
        Timber.d("onJobStatusChanged called($jobId, $jobStatus)")
        val sharedPrefs = SingletonInstances.getSharedPrefs()
        if (jobStatus == JobStatus.COMPLETED
                && !sharedPrefs.isRated
                && sharedPrefs.successJobsCount >= 3
                && System.currentTimeMillis() >= sharedPrefs.delayRateDialogUntil) {
            showRateDialog()
        }
    }

    inner class JobStatusChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val jobId = intent.getLongExtra(EXTRA_JOB_ID, ID_UNSET)
            val jobStatus = intent.getIntExtra(EXTRA_JOB_STATUS, -1)
            onJobStatusChanged(jobId, jobStatus)
        }
    }
}
