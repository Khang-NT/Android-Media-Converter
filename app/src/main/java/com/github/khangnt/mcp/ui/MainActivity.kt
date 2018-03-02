package com.github.khangnt.mcp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.MenuItem
import com.github.khangnt.mcp.ID_UNSET
import com.github.khangnt.mcp.PLAY_STORE_PACKAGE
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.SingletonInstances.Companion.getSharedPrefs
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.ui.jobmanager.JobManagerFragment
import com.github.khangnt.mcp.ui.presetcmd.PresetCommandFragment
import com.github.khangnt.mcp.util.appPermissions
import com.github.khangnt.mcp.util.hasWriteStoragePermission
import com.github.khangnt.mcp.util.openPlayStore
import com.github.khangnt.mcp.worker.ACTION_JOB_STATUS_CHANGED
import com.github.khangnt.mcp.worker.EXTRA_JOB_ID
import com.github.khangnt.mcp.worker.EXTRA_JOB_STATUS
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val EXTRA_OPEN_JOB_MANAGER = "EXTRA:openJobManager"
private const val KEY_SELECTED_FRAGMENT = "MainActivity:selectedFragment"

class MainActivity : SingleFragmentActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        fun openJobManagerIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .putExtra(EXTRA_OPEN_JOB_MANAGER, true)
        }
    }

    private val jobStatusChangedReceiver = JobStatusChangedReceiver()
    private var currentDrawerListener: DrawerLayout.DrawerListener? = null

    override fun onCreateLayout(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        navigationView.setNavigationItemSelectedListener(this)

        if (!hasWriteStoragePermission(this)) {
            // request permission without checking result
            requestPermissions(appPermissions, 0)
        }

        btnFeedback.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment {
        if (intent.hasExtra(EXTRA_OPEN_JOB_MANAGER)) {
            intent = intent.cloneFilter()
            return createSelectedFragment(R.id.item_nav_job_manager)
        }
        val selectedId = savedInstanceState?.getInt(KEY_SELECTED_FRAGMENT, R.id.item_nav_job_manager)
        return createSelectedFragment(selectedId ?: R.id.item_nav_job_manager)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(EXTRA_OPEN_JOB_MANAGER)) {
            this.intent = intent.cloneFilter()
            if (!navigationView.menu.findItem(R.id.item_nav_job_manager).isChecked) {
                replaceFragment(createSelectedFragment(R.id.item_nav_job_manager))
            }
        }
    }

    override fun getFragmentContainerId(): Int {
        return R.id.contentContainer
    }

    private fun createSelectedFragment(selectedId: Int): Fragment {
        navigationView.menu.findItem(selectedId)?.isChecked = true
        return when (selectedId) {
            R.id.item_nav_job_manager -> JobManagerFragment()
            R.id.item_nav_preset_command -> PresetCommandFragment()
            else -> throw IllegalArgumentException("Unknown selected fragment $selectedId")
        }
    }

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)
        toolbar?.let {
            currentDrawerListener?.let(drawerLayout::removeDrawerListener)
            currentDrawerListener = ActionBarDrawerToggle(this, drawerLayout, it,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close)
                    .also { actionBarDrawerToggle ->
                        drawerLayout.addDrawerListener(actionBarDrawerToggle)
                        actionBarDrawerToggle.syncState()
                    }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(jobStatusChangedReceiver, IntentFilter(ACTION_JOB_STATUS_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(jobStatusChangedReceiver)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        for (i in 0 until navigationView.menu.size()) {
            val menuItem = navigationView.menu.getItem(i)
            if (menuItem.isChecked) {
                outState.putInt(KEY_SELECTED_FRAGMENT, menuItem.itemId)
                break
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(Gravity.START)
        when (item.itemId) {
            R.id.item_nav_job_manager, R.id.item_nav_preset_command -> {
                if (!item.isChecked) replaceFragment(createSelectedFragment(item.itemId))
            }
            R.id.item_nav_about -> AboutActivity.launch(this)
            R.id.item_nav_setting -> {
                // todo: open settings page here
            }
        }
        return true
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
