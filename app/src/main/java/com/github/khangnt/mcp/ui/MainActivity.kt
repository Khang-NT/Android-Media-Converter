package com.github.khangnt.mcp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.github.khangnt.mcp.BuildConfig
import com.github.khangnt.mcp.PLAY_STORE_PACKAGE
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances.Companion.getSharedPrefs
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.ui.jobmanager.JobManagerFragment
import com.github.khangnt.mcp.ui.prefs.SettingsActivity
import com.github.khangnt.mcp.util.appPermissions
import com.github.khangnt.mcp.util.hasWriteStoragePermission
import com.github.khangnt.mcp.util.openPlayStore
import com.github.khangnt.mcp.util.viewChangelog
import com.github.khangnt.mcp.worker.ACTION_JOB_DONE
import com.github.khangnt.mcp.worker.EXTRA_JOB_ID
import com.github.khangnt.mcp.worker.EXTRA_JOB_STATUS
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_job_manager.*
import timber.log.Timber
import java.util.concurrent.TimeUnit.*

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

        val sharedPrefs = getSharedPrefs()
        if (sharedPrefs.lastKnownVersionCode < BuildConfig.VERSION_CODE) {
            viewChangelog(this)
            sharedPrefs.lastKnownVersionCode = BuildConfig.VERSION_CODE
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
                        it.setTag(R.id.toolbar_slide_drawable,
                                actionBarDrawerToggle.drawerArrowDrawable)
                    }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(jobStatusChangedReceiver, IntentFilter(ACTION_JOB_DONE))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(jobStatusChangedReceiver)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
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
        drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.item_nav_job_manager -> {
                if (!item.isChecked) replaceFragment(createSelectedFragment(item.itemId))
            }
            R.id.item_nav_about -> AboutActivity.launch(this)
            R.id.item_nav_setting -> SettingsActivity.launch(this)
        }
        return true
    }


    private fun showRateDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.rate_us_title)
                .setMessage(R.string.rate_us_message)
                .setPositiveButton(R.string.rate_us_love_it) { _, _ ->
                    openPlayStore(this, PLAY_STORE_PACKAGE)
                    getSharedPrefs().isRated = true
                }
                .setNeutralButton(R.string.rate_us_not_now) { _, _ ->
                    // don't show this dialog again, until next day
                    getSharedPrefs().delayRateDialogUntil =
                            System.currentTimeMillis() + MILLISECONDS.convert(1, DAYS)
                }
                .setNegativeButton(R.string.rate_us_never) { _, _ ->
                    getSharedPrefs().isRated = true
                }
                .show()
    }

    private fun onJobDone(jobId: Long, @JobStatus jobStatus: Int) {
        Timber.d("onJobDone called($jobId, $jobStatus)")
        val sharedPrefs = getSharedPrefs()
        if (jobStatus == JobStatus.COMPLETED
                && !sharedPrefs.isRated
                && sharedPrefs.successJobsCount >= 3
                && System.currentTimeMillis() >= sharedPrefs.delayRateDialogUntil) {
            sharedPrefs.delayRateDialogUntil =
                    System.currentTimeMillis() + MILLISECONDS.convert(1, HOURS)
            showRateDialog()
        }
    }

    inner class JobStatusChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val jobId = intent.getLongExtra(EXTRA_JOB_ID, 0)
            val jobStatus = intent.getIntExtra(EXTRA_JOB_STATUS, -1)
            onJobDone(jobId, jobStatus)
        }
    }
}
