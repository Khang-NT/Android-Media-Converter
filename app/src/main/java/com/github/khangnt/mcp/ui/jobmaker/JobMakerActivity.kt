package com.github.khangnt.mcp.ui.jobmaker

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.support.design.widget.BottomSheetBehavior
import android.view.Menu
import android.view.MenuItem
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.ui.BaseActivity
import com.github.khangnt.mcp.ui.filepicker.FileBrowserFragment
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel.Companion.STEP_CHOOSE_COMMAND
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.doOnPreDraw
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.activity_job_maker.*
import java.io.File

/**
 * Created by Khang NT on 4/5/18.
 * Email: khang.neon.1997@gmail.com
 */

class JobMakerActivity : BaseActivity(), FileBrowserFragment.Callbacks {

    private val TIME_INTERVAL = 2000
    private var mBackPressed: Long = 0

    private val bottomSheetBehavior by lazy { catchAll { BottomSheetBehavior.from(bottomSheetArea) } }
    private val fileBrowserFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fileBrowserContainer) as FileBrowserFragment
    }

    private val jobMakerViewModel by lazy { getViewModel<JobMakerViewModel>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_maker)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        if (savedInstanceState == null) {
            val fileBrowser = FileBrowserFragment.newInstance(getExternalStorageDirectory(),
                    limitSelectCount = Int.MAX_VALUE)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fileBrowserContainer, fileBrowser)
                    .replace(R.id.configurationContainer, JobMakerFragment())
                    .commit()
        }

        pathIndicatorView.onPathClick = { _, path -> fileBrowserFragment.goto(path) }

        bottomSheetBehavior?.let {
            bottomSheetArea?.doOnPreDraw {
                it.peekHeight += resources.getDimensionPixelOffset(R.dimen.margin_small)
            }
            if (savedInstanceState != null) {
                it.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        jobMakerViewModel.onResetEvent().observe {
            fileBrowserFragment.reset()
        }

        jobMakerViewModel.onRequestVisible().observe {
            // expand bottom sheet to show job maker fragment
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        jobMakerViewModel.getSelectedFiles().observe { selectedFiles ->
            if (selectedFiles !== fileBrowserFragment.getSelectedFiles()) {
                fileBrowserFragment.setSelectedFiles(selectedFiles)
            }
        }
    }

    override fun onSelectFilesChanged(files: List<File>) {
        jobMakerViewModel.setSelectedFiles(files)
    }

    override fun onCurrentDirectoryChanged(directory: File) {
        pathIndicatorView.setPath(directory)
    }

    override fun allowChangeSelectedFile(): Boolean {
        // block user to change selected file after STEP_CHOOSE_COMMAND
        return checkNotNull(jobMakerViewModel.getCurrentStep().value) <= STEP_CHOOSE_COMMAND
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_file_picker, menu)
        val isKitkat = Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT
        menu.findItem(R.id.item_goto_sd_card).isVisible = !isKitkat
                && SingletonInstances.getSdCardPath() != null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_goto_internal_storage -> {
                fileBrowserFragment.goto(Environment.getExternalStorageDirectory())
                return true
            }
            R.id.item_goto_sd_card -> {
                fileBrowserFragment.goto(checkNotNull(SingletonInstances.getSdCardPath()))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun consumeBackPress(): Boolean {
        super.consumeBackPress()
        // hide bottom sheet
        if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }

        // double tap back button to exit
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            return false
        } else {
            toast(getString(R.string.tap_again_to_exit))
        }

        mBackPressed = System.currentTimeMillis()

        return true
    }

}