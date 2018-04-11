package com.github.khangnt.mcp.ui.jobmaker

import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseActivity
import com.github.khangnt.mcp.ui.filepicker.FileBrowserFragment
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel.Companion.STEP_CHOOSE_COMMAND
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.doOnPreDraw
import com.github.khangnt.mcp.util.getSdCardPaths
import kotlinx.android.synthetic.main.activity_job_maker.*
import timber.log.Timber
import java.io.File

/**
 * Created by Khang NT on 4/5/18.
 * Email: khang.neon.1997@gmail.com
 */

class JobMakerActivity : BaseActivity(), FileBrowserFragment.Callbacks {

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
        }

        jobMakerViewModel.onResetEvent().observe {
            fileBrowserFragment.reset()
        }

        jobMakerViewModel.onRequestVisible().observe {
            // expand bottom sheet to show job maker fragment
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onSelectFilesChanged(files: List<File>) {
        jobMakerViewModel.setSelectedFiles(files.map { it.absolutePath })
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
        menu.findItem(R.id.item_goto_sd_card).isVisible = !isKitkat && sdCardPath.isNotEmpty()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_goto_internal_storage -> {
                fileBrowserFragment.goto(Environment.getExternalStorageDirectory())
                return true
            }
            R.id.item_goto_sd_card -> {
                if (sdCardPath.size == 1) {
                    fileBrowserFragment.goto(sdCardPath[0])
                } else {
                    val options = sdCardPath.map { it.absolutePath }.toTypedArray()
                    var selected = -1
                    AlertDialog.Builder(this)
                            .setTitle("Select external path")
                            .setSingleChoiceItems(options, -1, { _, which ->
                                selected = which
                            })
                            .setPositiveButton(R.string.action_ok, { _, _ ->
                                if (selected > -1) {
                                    fileBrowserFragment.goto(sdCardPath[selected])
                                }
                            })
                            .setNegativeButton(R.string.action_cancel, null)
                            .show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val sdCardPath: Array<File> by lazy {
        // try to find sd card path if any
        return@lazy try {
            getSdCardPaths(this).map { File(it) }.toTypedArray()
        } catch (error: Throwable) {
            Timber.d(error, "Failed to detect SD card")
            emptyArray<File>()
        }
    }

    override fun consumeBackPress(): Boolean {
        super.consumeBackPress()
        // hide bottom sheet
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        // always consume back press event
        // if user want to exit, they must use back button in toolbar instead
        return true
    }

}