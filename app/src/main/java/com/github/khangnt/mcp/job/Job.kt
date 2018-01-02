package com.github.khangnt.mcp.job

import com.github.khangnt.mcp.annotation.JobStatus

/**
 * Created by Khang NT on 12/30/17.
 * Email: khang.neon.1997@gmail.com
 */

data class Job(
        val id: Long,
        val title: String,
        @JobStatus val status: Long,
        val statusDetail: String?,
        val command: Command
)
