package com.github.khangnt.mcp.ui.jobmaker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel.Companion.STEP_ADVERTISEMENT
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel.Companion.STEP_SELECT_FILES
import com.github.khangnt.mcp.ui.jobmaker.selectfile.SelectedFilesFragment
import com.github.khangnt.mcp.ui.jobmaker.selectformat.ChooseCommandFragment
import com.github.khangnt.mcp.ui.jobmaker.selectoutput.ChooseOutputFragment
import com.github.khangnt.mcp.util.disableInHalfSecond
import com.github.khangnt.mcp.util.getViewModel
import kotlinx.android.synthetic.main.fragment_job_maker.*


class JobMakerFragment : BaseFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_job_maker, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var currentStep = checkNotNull(jobMakerViewModel.getCurrentStep().value)
        jobMakerViewModel.getCurrentStep().observe { step ->
            showStep(step, currentStep == step + 1)
            currentStep = step
        }

        jobMakerViewModel.onResetEvent().observe {
            jobMakerViewModel.setCurrentStep(STEP_SELECT_FILES)
            preventButtonClickToFast()
        }

        ivBack.setOnClickListener {
            jobMakerViewModel.setCurrentStep(currentStep - 1)
            preventButtonClickToFast()
        }
        ivNext.setOnClickListener {
            jobMakerViewModel.requestVisible()
            (childFragmentManager.findFragmentById(R.id.fragmentContainer) as? StepFragment)
                    ?.onGoToNextStep()
            preventButtonClickToFast()
        }
        peakArea.setOnClickListener {
            jobMakerViewModel.requestVisible()
        }
    }

    private fun preventButtonClickToFast() {
        if (ivBack.isEnabled) {
            ivBack.disableInHalfSecond()
        }
        ivNext.disableInHalfSecond()
    }

    private fun showStep(step: Int, reverseAnim: Boolean) {
        stepIndicator.setStep(step)
        if (step == STEP_SELECT_FILES || step == STEP_ADVERTISEMENT) {
            ivBack.alpha = 0.3f
            ivBack.isEnabled = false
        } else {
            ivBack.alpha = 1f
            ivBack.isEnabled = true
        }
        val tag = "step-$step"
        when (step) {
            JobMakerViewModel.STEP_SELECT_FILES -> {
                tvTitle.text = "Select File"
                showFragment(tag, reverseAnim) { SelectedFilesFragment() }
            }
            JobMakerViewModel.STEP_CHOOSE_COMMAND -> {
                tvTitle.text = "Choose command"
                showFragment(tag, reverseAnim) { ChooseCommandFragment() }
            }
            JobMakerViewModel.STEP_CONFIGURE_COMMAND -> {
                tvTitle.text = jobMakerViewModel.getSelectCommand().getTitle(resources)
                showFragment(tag, reverseAnim) { ConfigureCommandFragment() }
            }
            JobMakerViewModel.STEP_CHOOSE_OUTPUT_FOLDER_AND_REVIEW -> {
                tvTitle.text = "Choose output"
                showFragment(tag, reverseAnim) { ChooseOutputFragment() }
            }
            JobMakerViewModel.STEP_ADVERTISEMENT -> {
                tvTitle.text = "Create job success"
                showFragment(tag, reverseAnim) { AdvertiseFragment() }
            }
        }
    }

    private fun showFragment(tag: String, reverseAnim: Boolean, createFragment: () -> Fragment) {
        if (childFragmentManager.findFragmentByTag(tag) == null) {
            val fragment = createFragment()
            childFragmentManager.beginTransaction().apply {
                if (reverseAnim) {
                    setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                } else {
                    setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                replace(R.id.fragmentContainer, fragment, tag)
                commit()
            }
        }
    }

}
