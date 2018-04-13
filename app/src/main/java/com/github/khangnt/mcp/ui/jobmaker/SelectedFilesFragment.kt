package com.github.khangnt.mcp.ui.jobmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.util.getViewModel
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_todo.*

/**
 * Created by Khang NT on 4/6/18.
 * Email: khang.neon.1997@gmail.com
 */

class SelectedFilesFragment : StepFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_todo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        textView.text = """- Show list selected file
//- Have icon button (X) to unselected
//- Allow going to next step if select at least one file"""
    }

    override fun onGoToNextStep() {
        if (jobMakerViewModel.getSelectedFiles().value.orEmpty().isEmpty()) {
            toast("Select at least one file")
        } else {
            jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_CHOOSE_COMMAND)
        }
    }

}