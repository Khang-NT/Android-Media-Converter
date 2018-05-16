package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder

import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.util.parseFileName
import com.github.khangnt.mcp.util.parseInputUri
import timber.log.Timber

/**
 * Created by Khang NT on 4/6/18.
 * Email: khang.neon.1997@gmail.com
 */

abstract class CommandConfig(val inputFileUris: List<String>) {
    data class AutoGenOutput(val fileName: String, val fileExt: String)
    data class FinalOutput(val title: String, val outputUri: String)

    abstract fun getNumberOfOutput(): Int

    abstract fun generateOutputFiles(): List<AutoGenOutput>

    abstract fun makeJobs(finalFinalOutputs: List<FinalOutput>): List<Job>

    protected fun getFileNameFromInputs(index: Int): String {
        val inputFileName = inputFileUris[index].parseInputUri().lastPathSegment?.trim()
        if (inputFileName == null || inputFileName.isEmpty()) {
            return "Untitled"
        }
        val (name, extension) = inputFileName.parseFileName()
        Timber.d("File '$name' ext '$extension'")
        return name
    }

}