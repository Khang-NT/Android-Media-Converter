package com.github.khangnt.mcp.ui.jobmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.MainActivity.Companion.openJobManagerIntent
import com.github.khangnt.mcp.util.getViewModel
import kotlinx.android.synthetic.main.fragment_create_job_success.*

/**
 * Created by Khang NT on 4/11/18.
 * Email: khang.neon.1997@gmail.com
 */

class AdvertiseFragment : StepFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_create_job_success, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        textView.text = """- Show create job success
//- Show sponsored content (Native Ad)
//- Ad button (install app, go to website,...)
//- Button: Convert other files (go to step 1)
//- Button: View job manager
//"""
        btnContinue.setOnClickListener {
            onGoToNextStep()
        }

        btnExit.setOnClickListener {
            activity!!.finish()
            openJobManagerIntent(it.context)
        }
    }

    override fun onGoToNextStep() {
        // jobMakerViewModel.getCommandConfig().makeJobs(final outputs)
        jobMakerViewModel.postReset()
    }

}