package com.github.khangnt.mcp.ui.jobmaker

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.github.khangnt.mcp.ConvertCommand
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import com.github.khangnt.mcp.util.LiveEvent

class JobMakerViewModel : ViewModel() {

    companion object {
        const val STEP_SELECT_FILES = 1
        const val STEP_CHOOSE_COMMAND = 2
        const val STEP_CONFIGURE_COMMAND = 3
        const val STEP_CHOOSE_OUTPUT_FOLDER_AND_REVIEW = 4 // select folder and resolve file name conflict
        const val STEP_ADVERTISEMENT = 5  // show native ad at last
    }

    private val currentStepLiveData = MutableLiveData<Int>()
    private val selectedFilesLiveData = MutableLiveData<List<String>>()
    private val onResetLiveEvent = LiveEvent()
    private val requestVisibleLiveEvent = LiveEvent()

    private lateinit var selectedCommand: ConvertCommand
    private lateinit var commandConfig: CommandConfig

    init {
        currentStepLiveData.value = STEP_SELECT_FILES
        selectedFilesLiveData.value = emptyList()
    }

    fun getCurrentStep(): LiveData<Int> = currentStepLiveData

    fun setCurrentStep(step: Int) {
        check(step in STEP_SELECT_FILES..STEP_ADVERTISEMENT) { "Invalid step: $step" }
        currentStepLiveData.value = step
    }

    fun getSelectedFiles(): LiveData<List<String>> = selectedFilesLiveData

    fun setSelectedFiles(files: List<String>) {
        selectedFilesLiveData.value = files
    }

    fun setSelectedCommand(command: ConvertCommand) {
        selectedCommand = command
    }

    fun getSelectCommand(): ConvertCommand {
        check(checkNotNull(currentStepLiveData.value) > STEP_CHOOSE_COMMAND) {
            "Can't get selected command at current step: ${currentStepLiveData.value}"
        }
        return selectedCommand
    }

    fun setCommandConfig(config: CommandConfig) {
        commandConfig = config
    }

    fun getCommandConfig(): CommandConfig {
        check(checkNotNull(currentStepLiveData.value) > STEP_CONFIGURE_COMMAND) {
            "Can't get command config at current step: ${currentStepLiveData.value}"
        }
        return commandConfig
    }

    fun postReset() {
        onResetLiveEvent.fireEvent()
    }

    fun onResetEvent(): LiveEvent = onResetLiveEvent

    fun requestVisible() {
        requestVisibleLiveEvent.fireEvent()
    }

    fun onRequestVisible(): LiveEvent = requestVisibleLiveEvent

}