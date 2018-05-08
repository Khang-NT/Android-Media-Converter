package com.github.khangnt.mcp.ui.jobmaker.selectoutput

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.github.khangnt.mcp.util.getViewModel
import kotlinx.android.synthetic.main.fragment_choose_output.*
import java.io.File

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
            withModel<FileModel> {
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

        jobMakerViewModel.getSelectedFiles().observe { selectedFiles ->
            adapter.setData(selectedFiles.map { FileModel(it) })
//            tvEmptyMessage.visibility = if (selectedFiles.isEmpty()) View.VISIBLE else View.GONE
        }
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
            }
        }
    }

    override fun onGoToNextStep() {
        // jobMakerViewModel.getCommandConfig().makeJobs(final outputs)
        jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_ADVERTISEMENT)
    }

}