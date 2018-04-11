package com.github.khangnt.mcp.ui.jobmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.ConvertCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.util.getViewModel
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_todo.*

/**
 * Created by Khang NT on 4/7/18.
 * Email: khang.neon.1997@gmail.com
 */

class ChooseCommandFragment : StepFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_todo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textView.text = """- Show list preset command
- A preset command can be disabled if number inputs file is not suitable
- Select a enabled preset command to next""".trimIndent()

        textView.setOnClickListener {
            jobMakerViewModel.setSelectedCommand(ConvertCommand.CONVERT_MP3)
            jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_CONFIGURE_COMMAND)
        }

    }

    override fun onGoToNextStep() {
        toast("Select a command to continue (click text view)")
    }

}