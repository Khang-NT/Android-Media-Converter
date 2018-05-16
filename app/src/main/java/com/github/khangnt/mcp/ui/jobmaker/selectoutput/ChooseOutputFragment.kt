package com.github.khangnt.mcp.ui.jobmaker.selectoutput

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.provider.DocumentFile
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.R.id.edOutputPath
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.dialog.InputDialogFragment
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.filepicker.DIRECTORY_RESULT
import com.github.khangnt.mcp.ui.filepicker.FilePickerActivity
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel
import com.github.khangnt.mcp.ui.jobmaker.StepFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import com.github.khangnt.mcp.util.*
import io.fabric.sdk.android.services.settings.IconRequest.build
import kotlinx.android.synthetic.main.fragment_choose_output.*
import java.io.File


/**
 * Created by Khang NT on 4/11/18.
 * Email: khang.neon.1997@gmail.com
 */

class ChooseOutputFragment : StepFragment(), InputDialogFragment.Callbacks,
        InputDialogFragment.CheckInputCallback {

    companion object {
        private const val RC_PICK_DOCUMENT_TREE = 2
        private const val RC_PICK_FOLDER = 3

        private const val INPUT_DIALOG_TAG = "InputDialog"
        private const val EXTRA_OUTPUT_INDEX = "ChooseOutputFragment.OutputIndex"
        private const val EXTRA_INIT_VALUE = "ChooseOutputFragment.InitValue"
    }

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }
    private val chooseOutputViewModel by lazy { getViewModel<ChooseOutputViewModel>() }
    private val adapter: MixAdapter by lazy {
        MixAdapter.Builder {
            withModel<OutputFileAdapterModel> {
                ItemOutputFileViewHolder.Factory {
                    onEdit = { model, index ->
                        this@ChooseOutputFragment.onEdit(index, model.fileName)
                    }
                    onResolveConflict = { model, index ->
                        this@ChooseOutputFragment.onResolveConflict(index, model.fileName)
                    }
                }
            }
        }.build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chooseOutputViewModel.setCommandConfig(jobMakerViewModel.getCommandConfig())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_choose_output, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edOutputPath.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        .putExtra("android.content.extra.SHOW_ADVANCED", true)
                try {
                    startActivityForResult(intent, RC_PICK_DOCUMENT_TREE)
                    return@setOnClickListener
                } catch (ignore: ActivityNotFoundException) {
                }
            }
            val intent = FilePickerActivity.pickFolderIntent(it.context, ensureWritable = true)
            startActivityForResult(intent, RC_PICK_FOLDER)
        }
        updateOutputPath()

        recyclerView.adapter = adapter

        chooseOutputViewModel.getProcessingStatus().observe { processing ->
            if (processing) {
                progressBar.visible()
                recyclerView.invisible()
            } else {
                progressBar.gone()
                recyclerView.visible()
            }
        }

        chooseOutputViewModel.getListOutputFile().observe {
            adapter.setData(it)
        }
    }

    private fun updateOutputPath() {
        val outputFolderUri = chooseOutputViewModel.getOutputFolderUri()
        val path = catchAll { UriUtils.getDirectoryPathFromUri(outputFolderUri) }
        edOutputPath.setText(path ?: outputFolderUri.toString())
    }

    @SuppressLint("NewApi", "SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            RC_PICK_FOLDER -> {
                data?.getStringExtra(DIRECTORY_RESULT)?.let { path ->
                    val outputFolderUri = Uri.fromFile(File(path))
                    chooseOutputViewModel.setOutputFolderUri(outputFolderUri)
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
                }

                chooseOutputViewModel.setOutputFolderUri(uri)
            }
        }
        updateOutputPath()
    }

    override fun onGoToNextStep() {
        if (chooseOutputViewModel.getProcessingStatus().value == true) {
            return
        }

        chooseOutputViewModel.getListOutputFile().value!!.forEach {
            if (it.isConflict && !(it.isOverrideAllowed)) {
                toast(R.string.message_please_resolve_conflict)
                return
            }
        }

        val createFinalOutput: (fileName: String) -> CommandConfig.FinalOutput
        if (chooseOutputViewModel.getOutputFolderUri().scheme == ContentResolver.SCHEME_CONTENT) {
            val documentFile = DocumentFile.fromTreeUri(requireContext(),
                    chooseOutputViewModel.getOutputFolderUri())
            createFinalOutput = { fileName ->
                val uri = documentFile.createFile(null, fileName)
                CommandConfig.FinalOutput(fileName, uri.toString())
            }
        } else {
            val folder = File(chooseOutputViewModel.getOutputFolderUri().path)
            createFinalOutput = { fileName ->
                CommandConfig.FinalOutput(fileName, Uri.fromFile(File(folder, fileName)).toString())
            }
        }

        val finalOutputs = checkNotNull(chooseOutputViewModel.getListOutputFile().value).map {
            createFinalOutput(it.fileName)
        }

        jobMakerViewModel.getCommandConfig().makeJobs(finalOutputs)
        jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_ADVERTISEMENT)
    }

    override fun getInputError(dialog: InputDialogFragment, input: String): String? {
        if (dialog.arguments!!.getString(EXTRA_INIT_VALUE) == input) {
            return null
        }
        if (chooseOutputViewModel.getListFolderFileNames().contains(input)
                || chooseOutputViewModel.getListOutputFileNames().contains(input)) {
            return getString(R.string.file_exists)
        } else if (input.length < 3) {
            return getString(R.string.error_input_too_short)
        }
        return null
    }

    override fun onInputEntered(dialog: InputDialogFragment, finalInput: String) {
        val index = checkNotNull(dialog.arguments?.getInt(EXTRA_OUTPUT_INDEX, -1))
        chooseOutputViewModel.updateOutput(index, finalInput)
    }

    override fun onInputCancelled(dialog: InputDialogFragment) = Unit

    private fun onEdit(index: Int, initValue: String) {
        InputDialogFragment.Builder()
                .setEnableError(true)
                .setHint(getString(R.string.hint_file_name))
                .setTitle(getString(R.string.output_file_name_hint))
                .setInitValue(initValue)
                .setExtra(EXTRA_OUTPUT_INDEX, index)
                .setExtra(EXTRA_INIT_VALUE, initValue)
                .setMaxLines(1)
                .build()
                .show(childFragmentManager, INPUT_DIALOG_TAG)
    }

    private fun onResolveConflict(index: Int, currentFileName: String) {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.dialog_error_file_exists)
                .setMessage(getString(R.string.dialog_error_file_exists_message, currentFileName))
                .setCancelable(true)
                .setPositiveButton(R.string.action_rename, { _, _ ->
                    // show rename dialog
                    onEdit(index, currentFileName)
                })
                .setNegativeButton(R.string.action_override, { _, _ ->
                    chooseOutputViewModel.updateOutput(index, allowOverride = true)
                })
                .setNeutralButton(R.string.action_cancel, null)
                .show()
    }

}