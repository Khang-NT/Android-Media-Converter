package com.github.khangnt.mcp.ui.jobmaker

import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.ui.BaseActivity
import com.github.khangnt.mcp.ui.filepicker.FileBrowserFragment
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.doOnPreDraw
import kotlinx.android.synthetic.main.activity_job_maker.*
import timber.log.Timber
import java.io.File
import java.util.*

/**
 * Created by Khang NT on 4/5/18.
 * Email: khang.neon.1997@gmail.com
 */

class JobMakerActivity : BaseActivity(), FileBrowserFragment.Callbacks {

    private val bottomSheetBehavior by lazy { catchAll { BottomSheetBehavior.from(bottomSheetArea) } }
    private val fileBrowserFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fileBrowserContainer) as FileBrowserFragment
    }
    private val jobMakerFragment by  lazy {
        supportFragmentManager.findFragmentById(R.id.configurationContainer) as JobMakerFragment
    }

    private val jobMakerViewModel by lazy {
        ViewModelProviders.of(this, SingletonInstances.getViewModelFactory())
                .get(JobMakerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_maker)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        if (savedInstanceState == null) {
            val fileBrowser = FileBrowserFragment.newInstance(getExternalStorageDirectory(),
                    limitSelectCount = Int.MAX_VALUE)
            val jobMaker = JobMakerFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fileBrowserContainer, fileBrowser)
                    .replace(R.id.configurationContainer, jobMaker)
                    .commit()
        }

        pathIndicatorView.onPathClick = { _, path ->
            fileBrowserFragment.goto(path)
        }

        bottomSheetBehavior?.let {
            bottomSheetArea?.doOnPreDraw {
                it.peekHeight += resources.getDimensionPixelOffset(R.dimen.margin_small)
            }
        }
    }

    override fun onSelectFilesChanged(files: List<File>) {
        jobMakerViewModel.setSelectedFiles(files)
    }

    override fun onCurrentDirectoryChanged(directory: File) {
        pathIndicatorView.setPath(directory)
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
                    val selected = arrayOf(-1)
                    AlertDialog.Builder(this)
                            .setTitle("Select external path")
                            .setSingleChoiceItems(options, -1, { _, which ->
                                selected[0] = which
                            })
                            .setPositiveButton(R.string.action_ok, { _, _ ->
                                if (selected[0] > -1) {
                                    fileBrowserFragment.goto(sdCardPath[selected[0]])
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
            getStorageDirectories().map { File(it) }.toTypedArray()
        } catch (error: Throwable) {
            Timber.d(error, "Failed to detect SD card")
            emptyArray<File>()
        }
    }

    private fun getStorageDirectories(): List<String> {
        val rawSecondaryStorage = catchAll { System.getenv("SECONDARY_STORAGE") } ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val results = ArrayList<String>()
            val externalDirs = applicationContext.getExternalFilesDirs(null) ?: emptyArray()
            for (file in externalDirs) {
                if (!file.path.contains("/Android")) continue
                val path = file.path.split("/Android")[0]
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Environment.isExternalStorageRemovable(file)
                        || rawSecondaryStorage.contains(path)) {
                    results.add(path)
                }
            }
            return results
        } else {
            if (!TextUtils.isEmpty(rawSecondaryStorage)) {
                return rawSecondaryStorage.split(":")
                        .filter { it.isNotEmpty() }
            }
        }
        return emptyList()
    }

}