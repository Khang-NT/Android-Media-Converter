package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder

import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.util.parseFileName
import com.github.khangnt.mcp.util.parseInputUri
import timber.log.Timber

/**
 * Created by Khang NT on 4/6/18.
 * Email: khang.neon.1997@gmail.com
 */

abstract class CommandConfig(val inputFiles: List<String>) {
    data class Output(val title: String, val outputUri: String)

    abstract fun getNumberOfOutput(): Int

    abstract fun generateOutputFileNames(): List<Pair<String, String>>

    abstract fun makeJobs(finalOutputs: List<Output>): List<Job>

    fun getFileNameFromInputs(index: Int): String {
        val inputFileName = inputFiles.get(index).parseInputUri().lastPathSegment?.trim()
        if (inputFileName == null || inputFileName.isEmpty()) {
            return "Untitled"
        }
        val (name, extension) = inputFileName.parseFileName()
        Timber.d("File '$name' ext '$extension'")
        return name
    }

}