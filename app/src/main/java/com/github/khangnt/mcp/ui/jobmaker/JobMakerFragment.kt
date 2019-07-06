package com.github.khangnt.mcp.ui.jobmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel.Companion.STEP_ADVERTISEMENT
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel.Companion.STEP_CHOOSE_COMMAND
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel.Companion.STEP_SELECT_FILES
import com.github.khangnt.mcp.ui.jobmaker.selectfile.SelectedFilesFragment
import com.github.khangnt.mcp.ui.jobmaker.selectformat.ChooseCommandFragment
import com.github.khangnt.mcp.ui.jobmaker.selectoutput.ChooseOutputFragment
import com.github.khangnt.mcp.util.*
import kotlinx.android.synthetic.main.fragment_job_maker.*


class JobMakerFragment : BaseFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }
    private var nSelectedFiles = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_job_maker, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var currentStep = checkNotNull(jobMakerViewModel.getCurrentStep().value)
        jobMakerViewModel.getCurrentStep().observe { step ->
            showStep(step, currentStep == step + 1)
            currentStep = step
        }

        jobMakerViewModel.getSelectedFiles().observe {
            nSelectedFiles = it.size
            if (currentStep == STEP_SELECT_FILES || currentStep == STEP_CHOOSE_COMMAND) {
                showNumberSelectedFileTitle()
            }
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

    private fun showNumberSelectedFileTitle() {
        tvTitle.text = resources.getQuantityString(R.plurals.num_selected_file,
                nSelectedFiles, nSelectedFiles)
    }

    @SuppressLint("SetTextI18n")
    private fun showStep(step: Int, reverseAnim: Boolean) {
        stepIndicator.setStep(step)
        when (step) {
            STEP_SELECT_FILES -> {
                ivBack.visible()
                ivBack.alpha = 0.3f
                ivBack.isEnabled = false
            }
            STEP_ADVERTISEMENT -> ivBack.invisible()
            else -> {
                ivBack.visible()
                ivBack.alpha = 1f
                ivBack.isEnabled = true
            }
        }
        val tag = "step-$step"
        when (step) {
            JobMakerViewModel.STEP_SELECT_FILES -> {
                showNumberSelectedFileTitle()
                ivNext.visible()
                tvStart.gone()
                showFragment(tag, reverseAnim) { SelectedFilesFragment() }
            }
            JobMakerViewModel.STEP_CHOOSE_COMMAND -> {
                showNumberSelectedFileTitle()
                ivNext.invisible()
                tvStart.gone()
                showFragment(tag, reverseAnim) { ChooseCommandFragment() }
            }
            JobMakerViewModel.STEP_CONFIGURE_COMMAND -> {
                tvTitle.text = jobMakerViewModel.getSelectCommand().getTitle(resources)
                ivNext.visible()
                tvStart.gone()
                showFragment(tag, reverseAnim) { ConfigureCommandFragment() }
            }
            JobMakerViewModel.STEP_CHOOSE_OUTPUT_FOLDER_AND_REVIEW -> {
                tvTitle.text = getString(R.string.title_configure_output)
                tvStart.visible()
                showFragment(tag, reverseAnim) { ChooseOutputFragment() }
            }
            JobMakerViewModel.STEP_ADVERTISEMENT -> {
                tvTitle.text = getString(R.string.title_create_job_success)
                tvStart.gone()
                showFragment(tag, reverseAnim) { AdvertiseFragment() }
            }
        }
    }

    private fun showFragment(tag: String, reverseAnim: Boolean, createFragment: () -> Fragment) {
        val showingFragment = childFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (showingFragment?.tag == tag) {
            return
        }
        childFragmentManager.beginTransaction().apply {
            if (reverseAnim) {
                setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
            } else {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            val fragment = childFragmentManager.findFragmentByTag(tag) ?: createFragment()
            if (fragment.isDetached) {
                attach(fragment)
            } else {
                add(R.id.fragmentContainer, fragment, tag)
            }
            if (showingFragment != null) {
                detach(showingFragment)
            }
            commit()
        }
    }

}
