package com.github.khangnt.mcp.ui.jobmaker

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.PresetCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel.Companion.STEP_CHOOSE_OUTPUT_FOLDER_AND_REVIEW
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandBuilderFragment
import com.github.khangnt.mcp.util.getViewModel
import java.io.File

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class ConfigureCommandFragment : StepFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }
    private lateinit var commandBuilderFragment: CommandBuilderFragment

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.single_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedCommand = jobMakerViewModel.getSelectCommand()
        val selectedFiles = checkNotNull(jobMakerViewModel.getSelectedFiles().value)
        commandBuilderFragment = createCommandBuilderGui(selectedCommand, selectedFiles)
    }

    private fun createCommandBuilderGui(
            selectedCommand: PresetCommand,
            selectedFiles: List<File>
    ): CommandBuilderFragment {
        val currentFragment = childFragmentManager.findFragmentByTag(selectedCommand.getTag())
        if (currentFragment is CommandBuilderFragment) {
            return currentFragment
        }
        // create new one
        val newFragment = selectedCommand.createCommandBuilderFragment()
        newFragment.arguments = Bundle().apply {
            putStringArrayList(CommandBuilderFragment.ARG_INPUT_FILE_URIS,
                    ArrayList(selectedFiles.map { Uri.fromFile(it).toString() }))
        }
        childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, newFragment, selectedCommand.getTag())
                .commit()
        return newFragment
    }

    override fun onGoToNextStep() {
        commandBuilderFragment.validateConfig { commandConfig ->
            jobMakerViewModel.setCommandConfig(commandConfig)
            jobMakerViewModel.setCurrentStep(STEP_CHOOSE_OUTPUT_FOLDER_AND_REVIEW)
        }
    }

}