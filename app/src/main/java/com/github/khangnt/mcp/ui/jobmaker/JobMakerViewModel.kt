package com.github.khangnt.mcp.ui.jobmaker

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.net.Uri
import android.support.annotation.MainThread
import com.github.khangnt.mcp.PresetCommand2
import com.github.khangnt.mcp.util.checkMainThread
import java.io.File

class JobMakerViewModel : ViewModel() {

    companion object {
        const val STEP_SELECT_FILES = 0
        const val STEP_CHOOSE_FORMAT = 1
        const val STEP_FORMAT_SETTING = 2
        const val STEP_CHOOSE_OUTPUT_AND_REVIEW = 3 // select folder and resolve file name conflict
        const val STEP_ADVERTISEMENT = 4  // show native ad at last
    }

    private val selectedFilesLiveData = MutableLiveData<List<File>>()

    init {
        selectedFilesLiveData.value = emptyList()
    }

    fun getCurrentStep(): LiveData<Int> {
        TODO()
    }

    fun setCurrentStep(step: Int): Unit {
        TODO()
    }

    fun getSelectedFiles(): LiveData<List<File>> = selectedFilesLiveData

    @MainThread
    fun setSelectedFiles(files: List<File>) {
        checkMainThread("setSelectedFiles")
        selectedFilesLiveData.value = files
    }

    fun setSelectedPreset(presetCommand: PresetCommand2) {
        TODO()
    }

    fun getSelectedPreset(): PresetCommand2? {
        TODO()
    }

    fun setCommandConfig(commandConfig: CommandConfig) {
        TODO()
    }

    fun getCommandConfig(): CommandConfig? {
        TODO()
    }

    fun setOutputFolder(uri: String) {
        TODO()
    }

    fun getOutputFolder(): Uri? {
        TODO()
    }

    fun setOutputFileNames(list: List<String>) {
        TODO()
    }

    fun getOutputFileNames(): List<String>? {
        TODO()
    }

}