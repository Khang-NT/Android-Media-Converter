package com.github.khangnt.mcp.ui.jobmaker

import com.github.khangnt.mcp.db.job.Job
import java.io.File

/**
 * Created by Khang NT on 4/6/18.
 * Email: khang.neon.1997@gmail.com
 */

abstract class CommandConfig {

    abstract fun getNumberOfOutput(inputs: List<File>): Int

    abstract fun generateOutputFileNames(inputFileNames: List<String>): List<String>

    abstract fun makeJobs(inputs: List<File>, outputs: List<String>): List<Job>

}