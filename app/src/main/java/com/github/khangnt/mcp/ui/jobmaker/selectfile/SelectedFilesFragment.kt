package com.github.khangnt.mcp.ui.jobmaker.selectfile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel
import com.github.khangnt.mcp.ui.jobmaker.StepFragment
import com.github.khangnt.mcp.util.getViewModel
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_selected_files.*
import java.io.File

/**
 * Created by Khang NT on 4/6/18.
 * Email: khang.neon.1997@gmail.com
 */

class SelectedFilesFragment : StepFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }
    private val adapter: MixAdapter by lazy {
        MixAdapter.Builder {
            withModel<FileModel> { ItemFileViewHolder.Factory() }
        }.build()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_selected_files, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter

        jobMakerViewModel.getSelectedFiles().observe { selectedFiles ->
            adapter.setData(selectedFiles.map { FileModel(File(it)) })
            tvEmptyMessage.visibility = if (selectedFiles.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onGoToNextStep() {
        if (jobMakerViewModel.getSelectedFiles().value.orEmpty().isEmpty()) {
            toast(R.string.empty_selected_files)
        } else {
            jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_CHOOSE_COMMAND)
        }
    }

}