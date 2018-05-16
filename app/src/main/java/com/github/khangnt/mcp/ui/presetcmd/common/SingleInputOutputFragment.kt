package com.github.khangnt.mcp.ui.presetcmd.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
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
import com.github.khangnt.mcp.util.*
import kotlinx.android.synthetic.main.fragment_single_input_output.*
import java.io.File


data class InputOutputData(val title: String, val inputUri: String, val outputUri: String)

class SingleInputOutputFragment : BaseFragment() {

    companion object {
        private const val RC_PICK_INPUT_FILE = 1
        private const val RC_PICK_DOCUMENT_TREE = 2
        private const val RC_PICK_FOLDER = 3

        private const val KEY_OUTPUT_FOLDER = "SingleInputOutputFragment:outputFolder"
    }

    private val sharedPrefs = SingletonInstances.getSharedPrefs()
    private var outputFolderUri: Uri? = null

    var autoFillExt: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState !== null) {
            outputFolderUri = savedInstanceState.getString(KEY_OUTPUT_FOLDER)?.let { Uri.parse(it) }
        } else {
            outputFolderUri = sharedPrefs.lastOutputFolderUri?.let { Uri.parse(it) }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_single_input_output, container, false)

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        .putExtra("android.content.extra.SHOW_ADVANCED", outputFolderUri === null)
                try {
                    startActivityForResult(intent, RC_PICK_DOCUMENT_TREE)
                    return@setOnClickListener
                } catch (ignore: ActivityNotFoundException) {
                }
            }
            val intent = FilePickerActivity.pickFolderIntent(it.context, ensureWritable = true)
            startActivityForResult(intent, RC_PICK_FOLDER)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_OUTPUT_FOLDER, outputFolderUri?.toString())
    }

    /**
     * Validate input and output, then invoke [callback] with valid [InputOutputData].
     * [callback] never be invoked if input or output invalid.
     */
    fun validateAndGetInputOutputData(callback: (InputOutputData) -> Unit) {
        // validate
        getInput0Error().apply { edInput0.error = this }?.also { return }
        getOutputFolderError()?.apply { edOutputPath.error = this }?.also { return }
        getOutputFileNameError()?.apply { edOutputName.error = this }?.also { return }

        val inputUri = edInput0.text.toString().parseInputUri().toString()
        val fileName = edOutputName.text.toString()

        val existingFile = context!!.checkFileExists(outputFolderUri!!, fileName)?.toString()
        if (existingFile != null) {
            // ask user whether they want to override this file or change file name
            AlertDialog.Builder(context!!)
                    .setTitle(R.string.dialog_error_file_exists)
                    .setMessage(getString(R.string.dialog_error_file_exists_message, fileName))
                    .setCancelable(false)
                    .setPositiveButton(R.string.action_override, { _, _ ->
                        // override existing file
                        callback(InputOutputData(fileName, inputUri, existingFile))
                    })
                    .setNegativeButton(R.string.action_rename, { _, _ ->
                        // discard action, let user change output name
                        edOutputName.openKeyboard()
                    })
                    .show()
            return
        }

        val outputUri: Uri = when {
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
        callback(InputOutputData(fileName, inputUri, outputUri.toString()))
    }

    fun shouldConfirmDiscardChanges(): Boolean {
        return edOutputName.text.isNotEmpty() ||
                edInput0.text.isNotEmpty() ||
                outputFolderUri?.toString() != sharedPrefs.lastOutputFolderUri
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
                        edOutputName.setText("${File(it).nameWithoutExtension}.${autoFillExt!!}")
                    }
                }
            }
            RC_PICK_FOLDER -> {
                data?.getStringExtra(DIRECTORY_RESULT)?.let { path ->
                    outputFolderUri = Uri.fromFile(File(path))
                    edOutputPath.setText(path)
                }
            }
            RC_PICK_DOCUMENT_TREE -> {
                // >= LOLLIPOP only
                val uri = data!!.data
                val takeFlags = data.flags and
                        (FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION)
                if (data.flags and FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        == FLAG_GRANT_PERSISTABLE_URI_PERMISSION) {
                    context!!.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    sharedPrefs.lastOutputFolderUri = uri.toString()
                }

                outputFolderUri = uri

                val path = catchAll { UriUtils.getDirectoryPathFromUri(uri) }
                edOutputPath.setText(path ?: uri.toString())
            }
        }
    }

    private fun getInput0Error(): String? {
        val uriString = edInput0.text.toString()
        if (uriString.isEmpty()) {
            return getString(R.string.error_input_too_short)
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