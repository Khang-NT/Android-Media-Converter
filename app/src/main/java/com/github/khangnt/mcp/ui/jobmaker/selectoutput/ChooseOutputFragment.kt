package com.github.khangnt.mcp.ui.jobmaker.selectoutput

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.filepicker.DIRECTORY_RESULT
import com.github.khangnt.mcp.ui.filepicker.FilePickerActivity
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel
import com.github.khangnt.mcp.ui.jobmaker.StepFragment
import com.github.khangnt.mcp.util.UriUtils
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.checkFileExists
import com.github.khangnt.mcp.util.getViewModel
import kotlinx.android.synthetic.main.fragment_choose_output.*
import java.io.File
import android.widget.LinearLayout
import android.widget.EditText


/**
 * Created by Khang NT on 4/11/18.
 * Email: khang.neon.1997@gmail.com
 */

class ChooseOutputFragment : StepFragment() {

    companion object {
        private const val RC_PICK_DOCUMENT_TREE = 2
        private const val RC_PICK_FOLDER = 3
    }

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }
    private val adapter: MixAdapter by lazy {
        MixAdapter.Builder {
            withModel<OutputFile> {
                ItemOutputFileViewHolder.Factory {
//                    onStartDrag = { itemTouchHelper.startDrag(it) }
//                    onRemoveFile = { jobMakerViewModel.removeSelectedFiles(it) }
                }
            }
        }.build()
    }

    private var outputFolderUri: Uri? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_choose_output, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        textView.text = """- Pick output folder
//- Show list output file
//- Highlight conflict output file with resolve options (rename, override)
//"""
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


        recyclerView.adapter = adapter

        val outputFileNames = jobMakerViewModel.getCommandConfig().generateOutputFileNames()
        val outputFileExt = jobMakerViewModel.getCommandConfig().getOutputFileNameExt()

        adapter.setData(outputFileNames.map { OutputFile(it, outputFileExt) })

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
                    outputFolderUri = Uri.fromFile(File(path))
                    edOutputPath.setText(path)
                    edOutputPath.error = null
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

                outputFolderUri = uri

                val path = catchAll { UriUtils.getDirectoryPathFromUri(uri) }
                edOutputPath.setText(path ?: uri.toString())
                edOutputPath.error = null
            }
        }
    }

    override fun onGoToNextStep() {
        getOutputFolderError()?.apply { edOutputPath.error = this }?.also { return }

        jobMakerViewModel.getCommandConfig().generateOutputFileNames().forEach { fileName ->
            val existingFile = context!!.checkFileExists(outputFolderUri!!, fileName)?.toString()

            if (existingFile != null) {
                // ask user whether they want to override this file or change file name
                AlertDialog.Builder(context!!)
                        .setTitle(R.string.dialog_error_file_exists)
                        .setMessage(getString(R.string.dialog_error_file_exists_message, fileName))
                        .setCancelable(false)
                        .setPositiveButton(R.string.action_override, { _, _ ->
                            // override existing file??
                        })
                        .setNegativeButton(R.string.action_rename, { _, _ ->
                            // show edit name dialog
                            editOutputFileName(fileName)
                        })
                        .show()
                return
            }
        }

        // jobMakerViewModel.getCommandConfig().makeJobs(final outputs)
        jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_ADVERTISEMENT)
    }

    private fun getOutputFolderError(): String? {
        if (outputFolderUri === null) {
            return getString(R.string.error_output_folder_empty)
        }
        return null
    }

    private fun editOutputFileName(fileName: String) {
        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        lp.setMargins(8,8,8,8)
        input.setText(fileName)

        // ask user to enter new file name
        AlertDialog.Builder(context!!)
                .setTitle("Please enter new file name")
                .setCancelable(true)
                .setPositiveButton(R.string.action_rename, { _, _ ->
                    // rename the file
                })
                .setNegativeButton("Cancel", { _, _ ->
                    // nothing to do
                })
                .setView(input)
                .show()
        return
    }
}