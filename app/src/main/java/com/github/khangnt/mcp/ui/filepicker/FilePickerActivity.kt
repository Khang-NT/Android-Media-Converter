package com.github.khangnt.mcp.ui.filepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.SingleFragmentActivity
import com.github.khangnt.mcp.util.catchAll
import kotlinx.android.synthetic.main.activity_file_picker.*
import timber.log.Timber
import java.io.File
import java.util.*


/**
 * Created by Khang NT on 1/30/18.
 * Email: khang.neon.1997@gmail.com
 */

private const val EXTRA_PICK_TYPE = "FilePickerActivity:pick_type"
private const val EXTRA_MAX_FILE_CAN_PICK = "FilePickerActivity:max_file_can_pick"
private const val EXTRA_START_UP_DIRECTORY = "FilePickerActivity:start_up_directory"
private const val EXTRA_ENSURE_FOLDER_WRITABLE = "FilePickerActivity:ensure_folder_writable"
private const val EXTRA_ENSURE_FOLDER_READABLE = "FilePickerActivity:ensure_folder_readable"

private const val TYPE_PICK_FOLDER = 0
private const val TYPE_PICK_FILE = 1

private const val KEY_BTN_SELECT_ENABLED = "key:btnSelectEnabled"

const val FILES_RESULT = "FilePickerActivity:files_result"
const val DIRECTORY_RESULT = "FilePickerActivity:directory_result"

class FilePickerActivity : SingleFragmentActivity() {

    companion object {
        fun pickFolderIntent(
                context: Context,
                startUpDir: File = Environment.getExternalStorageDirectory(),
                ensureReadable: Boolean = true,
                ensureWritable: Boolean = false
        ): Intent {
            return Intent(context, FilePickerActivity::class.java)
                    .putExtra(EXTRA_PICK_TYPE, TYPE_PICK_FOLDER)
                    .putExtra(EXTRA_START_UP_DIRECTORY, startUpDir.absolutePath)
                    .putExtra(EXTRA_ENSURE_FOLDER_READABLE, ensureReadable)
                    .putExtra(EXTRA_ENSURE_FOLDER_WRITABLE, ensureWritable)
        }

        fun pickFileIntent(
                context: Context,
                startUpDir: File? = null,
                maxFileCanPick: Int = 1
        ): Intent {
            check(maxFileCanPick > 0, { "maxFileCanPick must greater than 0" })
            val startUpDirNonNull = (startUpDir ?: Environment.getExternalStorageDirectory())
            return Intent(context, FilePickerActivity::class.java)
                    .putExtra(EXTRA_PICK_TYPE, TYPE_PICK_FILE)
                    .putExtra(EXTRA_START_UP_DIRECTORY, startUpDirNonNull.absolutePath)
                    .putExtra(EXTRA_MAX_FILE_CAN_PICK, maxFileCanPick)
        }
    }

    private val isPickFile by lazy { intent.getIntExtra(EXTRA_PICK_TYPE, 0) == TYPE_PICK_FILE }
    private val maxFileCanPick by lazy { intent.getIntExtra(EXTRA_MAX_FILE_CAN_PICK, 0) }
    private val ensureReadable by lazy { intent.getBooleanExtra(EXTRA_ENSURE_FOLDER_READABLE, true) }
    private val ensureWritable by lazy { intent.getBooleanExtra(EXTRA_ENSURE_FOLDER_READABLE, false) }
    private val startUpDir: File by lazy {
        val dir = intent.getStringExtra(EXTRA_START_UP_DIRECTORY)?.let(::File)
        if (dir?.exists() == true) {
            if (dir.isDirectory) dir else dir.parentFile
        } else {
            Environment.getExternalStorageDirectory()
        }
    }

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment {
        return FileBrowserFragment.newInstance(startUpDir, if (isPickFile) maxFileCanPick else 0)
    }

    override fun onCreateLayout(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_file_picker)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        title = if (isPickFile) getString(R.string.pick_file) else getString(R.string.pick_folder)

        // set result cancelled as default, only change when click "Select" button
        setResult(Activity.RESULT_CANCELED)
    }

    override fun onFragmentCreated(fragment: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fragment, savedInstanceState)
        if (savedInstanceState !== null) {
            btnSelect.isEnabled = savedInstanceState.getBoolean(KEY_BTN_SELECT_ENABLED)
        } else {
            btnSelect.isEnabled = false
        }

        val fileBrowserFragment = fragment as FileBrowserFragment
        fileBrowserFragment.onSelectedFilesChanged = { _, files ->
            check(isPickFile == true)
            btnSelect.isEnabled = files.size == maxFileCanPick
        }
        fileBrowserFragment.onDirChanged = { _, dir ->
            pathIndicatorView.setPath(dir)
            if (!isPickFile) {
                btnSelect.isEnabled = catchAll {
                    (!ensureReadable || dir.canRead())
                            && (!ensureWritable || dir.canWrite())
                } ?: false
            }
        }
        pathIndicatorView.onPathClick = { _, dir ->
            fileBrowserFragment.goto(dir)
        }

        btnCancel.setOnClickListener { finish() }
        btnSelect.setOnClickListener {
            val result = Intent()
            if (isPickFile) {
                result.putStringArrayListExtra(FILES_RESULT,
                        ArrayList(fileBrowserFragment.getCheckedFiles().map { it.absolutePath }))
            } else {
                result.putExtra(DIRECTORY_RESULT, fileBrowserFragment.getCurrentDir()!!.absolutePath)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_BTN_SELECT_ENABLED, btnSelect.isEnabled)
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun getFragmentContainerId(): Int {
        return R.id.fileBrowserContainer
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_file_picker, menu)
        val isKitkat = SDK_INT == Build.VERSION_CODES.KITKAT
        menu.findItem(R.id.item_goto_sd_card).isVisible = !isKitkat && sdCardPath.isNotEmpty()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_goto_internal_storage -> {
                (getContentFragment() as? FileBrowserFragment)
                        ?.goto(Environment.getExternalStorageDirectory())
                return true
            }
            R.id.item_goto_sd_card -> {
                val fragment = (getContentFragment() as? FileBrowserFragment)
                if (sdCardPath.size == 1) {
                    fragment?.goto(sdCardPath[0])
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
                                    fragment?.goto(sdCardPath[selected[0]])
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

        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val results = ArrayList<String>()
            val externalDirs = applicationContext.getExternalFilesDirs(null) ?: emptyArray()
            for (file in externalDirs) {
                if (!file.path.contains("/Android")) continue
                val path = file.path.split("/Android")[0]
                if (SDK_INT >= LOLLIPOP && Environment.isExternalStorageRemovable(file)
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