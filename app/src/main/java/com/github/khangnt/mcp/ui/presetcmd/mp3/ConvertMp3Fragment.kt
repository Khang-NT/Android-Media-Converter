package com.github.khangnt.mcp.ui.presetcmd.mp3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.provider.DocumentFile
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.filepicker.DIRECTORY_RESULT
import com.github.khangnt.mcp.ui.filepicker.FILES_RESULT
import com.github.khangnt.mcp.ui.filepicker.FilePickerActivity
import com.github.khangnt.mcp.ui.presetcmd.ConvertActivity
import com.github.khangnt.mcp.ui.presetcmd.isChanged
import com.github.khangnt.mcp.util.*
import com.github.khangnt.mcp.worker.ConverterService
import kotlinx.android.synthetic.main.fragment_convert_mp3.*
import java.io.File


/**
 * GUI helps create convert mp3 command, likes:
 * ffmpeg -i input -codec:a libmp3lame -q:a 0 -f mp3 output.mp3
 * Or with libshine encoder:
 * ffmpeg -i input -codec:a libshine -b:a 256k mp3 output.mp3
 */
class ConvertMp3Fragment : BaseFragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_mp3, container, false)

    companion object {
        private const val RC_PICK_INPUT_FILE = 1
        private const val RC_PICK_DOCUMENT_TREE = 2
        private const val RC_PICK_FOLDER = 3

        // https://trac.ffmpeg.org/wiki/Encode/MP3
        private val libMp3LameQuality = arrayOf(
                "220-260", "190-250", "170-210", "150-195", "140-185",
                "120-150", "100-130", "80-120", "70-105", "45-85"
        )

        private val cbrMin = 45  // 45 kbps
        private val cbrMax = 320 // 320 kbps
    }

    private val sharedPrefs = SingletonInstances.getSharedPrefs()
    private var outputFolderUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        isChanged = false

        outputFolderUri = sharedPrefs.lastOutputFolderUri?.let { Uri.parse(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState === null) {
            outputFolderUri?.let { uri ->
                val path = catchAll { UriUtils.getDirectoryPathFromUri(uri) }
                edOutputPath.setText(path ?: uri.toString())
            }
        }
        ibPickFile.setOnClickListener {
            val startUpDir = if (getInput0Error() === null) {
                File(edInput0.text.toString().parseInputUri().path)
            } else null
            val pickFileIntent = FilePickerActivity.pickFileIntent(it.context, maxFileCanPick = 1,
                    startUpDir = startUpDir)
            startActivityForResult(pickFileIntent, RC_PICK_INPUT_FILE)
        }
        edOutputPath.setOnClickListener {
            val requestCode: Int
            val pickOutputFolderIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                requestCode = RC_PICK_DOCUMENT_TREE
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        .putExtra("android.content.extra.SHOW_ADVANCED", outputFolderUri === null)
            } else {
                requestCode = RC_PICK_FOLDER
                FilePickerActivity.pickFolderIntent(it.context, ensureWritable = true)
            }
            startActivityForResult(pickOutputFolderIntent, requestCode)
        }
        sbQuality.onSeekBarChanged { updateQualityText() }
        spinnerEncoder.onItemSelected { position ->
            when (position) {
                0 -> {
                    // libMp3lame
                    if (sbQuality.max != 9) {
                        sbQuality.progress = 9
                        sbQuality.max = 9
                    }
                }
                1 -> {
                    if (sbQuality.max != cbrMax - cbrMin) {
                        sbQuality.max = cbrMax - cbrMin
                        sbQuality.progress = 256 - cbrMin
                    }
                }
            }
        }

        btnStartConversion.setOnClickListener { checkSettingsAndStartConversion() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateQualityText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
        if (spinnerEncoder.selectedItemPosition == 0 && sbQuality.progress <= 9) {
            tvQualityValue.text = "${libMp3LameQuality[9 - sbQuality.progress]} kbps"
        } else {
            tvQualityValue.text = "${sbQuality.progress + cbrMin} kbps"
        }
    }

    @SuppressLint("NewApi", "SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            RC_PICK_INPUT_FILE -> {
                data?.getStringArrayListExtra(FILES_RESULT)?.firstOrNull()?.let {
                    edInput0.setText(it)
                    edInput0.error = null

                    if (edOutputName.text.isEmpty()) {
                        // auto fill output file name
                        edOutputName.setText("${File(it).nameWithoutExtension}.mp3")
                        isChanged = true
                    }
                }
            }
            RC_PICK_FOLDER -> {
                data?.getStringExtra(DIRECTORY_RESULT)?.let { path ->
                    outputFolderUri = Uri.fromFile(File(path))
                    edOutputPath.setText(path)
                    isChanged = true
                }
            }
            RC_PICK_DOCUMENT_TREE -> {
                // >= LOLLIPOP only
                val uri = data!!.data
                val takeFlags = data.flags and
                        (FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION)
                context!!.contentResolver.takePersistableUriPermission(uri, takeFlags)
                outputFolderUri = uri
                val path = catchAll { UriUtils.getDirectoryPathFromUri(uri) }
                edOutputPath.setText(path ?: uri.toString())
                isChanged = true
            }
        }
    }

    private fun checkSettingsAndStartConversion(overrideExistingFile: Uri? = null) {
        // Check settings
        getInput0Error().apply { edInput0.error = this }?.also { return }
        getOutputFolderError()?.apply { edOutputPath.error = this }?.also { return }
        getOutputFileNameError()?.apply { edOutputName.error = this }?.also { return }

        val cmdArgsBuilder = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g -codec:a ")
        if (spinnerEncoder.selectedItemPosition == 0) {
            cmdArgsBuilder.append("libmp3lame -q:a ${9 - sbQuality.progress} ")
        } else {
            cmdArgsBuilder.append("libshine -b:a ${cbrMin + sbQuality.progress}k ")
        }

        val fileName = edOutputName.text.toString()
        if (overrideExistingFile == null) {
            val existingFile = context!!.checkFileExists(outputFolderUri!!, fileName)
            if (existingFile != null) {
                // ask user override this file or change file name
                AlertDialog.Builder(context!!)
                        .setTitle(R.string.dialog_error_file_exists)
                        .setMessage(getString(R.string.dialog_error_file_exists_message, fileName))
                        .setPositiveButton(R.string.action_override, { _, _ ->
                            checkSettingsAndStartConversion(existingFile)
                        })
                        .setNegativeButton(R.string.action_rename, { _, _ ->
                            edOutputName.openKeyboard()
                        })
                        .show()
                return
            }
        }

        val outputUri: Uri = when {
            overrideExistingFile != null -> overrideExistingFile
            outputFolderUri!!.scheme == "file" -> Uri.fromFile(File(outputFolderUri!!.path, fileName))
            else -> {
                try {
                    val documentTree = DocumentFile.fromTreeUri(context!!, outputFolderUri!!)
                    documentTree.createFile(null, fileName).uri
                } catch (error: Throwable) {
                    toast(getString(R.string.error_can_not_create_output_file, error.message))
                    return
                }
            }
        }

        sharedPrefs.lastOutputFolderUri = outputFolderUri?.toString()

        ConverterService.newJob(
                context!!,
                title = edOutputName.text.toString(),
                inputs = listOf(edInput0.text.toString().parseInputUri().toString()),
                args = cmdArgsBuilder.toString(),
                outputUri = outputUri.toString(),
                outputFormat = "mp3"
        )

        (activity as? ConvertActivity)?.setResult(Activity.RESULT_OK)
        (activity as? ConvertActivity)?.finish()
    }

    private fun getInput0Error(): String? {
        val uriString = edInput0.text.toString()
        if (uriString.isEmpty()) {
            return getString(R.string.error_input_text_empty)
        }
        val uri = uriString.parseInputUri()
        when (uri.scheme?.toLowerCase()) {
            "file" -> {
                val file = File(uri.path)
                if (!file.exists()) {
                    return getString(R.string.error_file_not_exists)
                } else if (!file.isFile) {
                    return getString(R.string.error_not_a_file)
                }
            }
            else -> {
                if (!uri.isHierarchical || uri.scheme.isNullOrEmpty() || uri.host.isNullOrEmpty()) {
                    return getString(R.string.error_invalid_url)
                }
            }
        }
        return null
    }

    private fun getOutputFolderError(): String? {
        if (outputFolderUri === null) {
            return getString(R.string.error_output_folder_empty)
        }
        return null
    }

    private fun getOutputFileNameError(): String? {
        if (edOutputName.text.isEmpty()) {
            return getString(R.string.error_file_name_empty)
        }
        return null
    }

}