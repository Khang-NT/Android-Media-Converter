package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder

import com.github.khangnt.mcp.db.job.Job

/**
 * Created by Khang NT on 4/6/18.
 * Email: khang.neon.1997@gmail.com
 */

abstract class CommandConfig(val inputFiles: List<String>) {
    data class Output(val title: String, val outputUri: String)

    abstract fun getNumberOfOutput(): Int

    abstract fun generateOutputFileNames(): List<String>

    abstract fun getOutputFileNameExt(): String

    abstract fun makeJobs(finalOutputs: List<Output>): List<Job>

}