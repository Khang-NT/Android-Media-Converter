package com.github.khangnt.mcp.ui.jobmaker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.khangnt.mcp.PresetCommand
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import com.github.khangnt.mcp.util.LiveEvent
import java.io.File
import java.util.*

class JobMakerViewModel : ViewModel() {

    companion object {
        const val STEP_SELECT_FILES = 1
        const val STEP_CHOOSE_COMMAND = 2
        const val STEP_CONFIGURE_COMMAND = 3
        const val STEP_CHOOSE_OUTPUT_FOLDER_AND_REVIEW = 4 // select folder and resolve file name conflict
        const val STEP_ADVERTISEMENT = 5  // show native ad at last
    }

    private val currentStepLiveData = MutableLiveData<Int>()
    private val selectedFilesLiveData = MutableLiveData<List<File>>()
    private val onResetLiveEvent = LiveEvent()
    private val requestVisibleLiveEvent = LiveEvent()

    private lateinit var selectedCommand: PresetCommand
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

    fun getSelectedFiles(): LiveData<List<File>> = selectedFilesLiveData

    fun setSelectedFiles(files: List<File>) {
        selectedFilesLiveData.value = files
    }

    fun moveSelectedFiles(fromPos: Int, toPos: Int) {
        val newList = ArrayList(selectedFilesLiveData.value)
        newList.add(toPos, newList.removeAt(fromPos))
        selectedFilesLiveData.value = newList
    }

    fun removeSelectedFiles(file: File) {
        val newList = ArrayList(selectedFilesLiveData.value)
        newList.remove(file)
        selectedFilesLiveData.value = newList
    }

    fun setSelectedCommand(command: PresetCommand) {
        selectedCommand = command
    }

    fun getSelectCommand(): PresetCommand {
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