package com.github.khangnt.mcp.job

import com.github.khangnt.mcp.annotation.JobStatus

/**
 * Created by Khang NT on 12/30/17.
 * Email: khang.neon.1997@gmail.com
 */

data class Job(
        val id: Long,
        val title: String,
        @JobStatus val status: Int,
        val statusDetail: String?,
        val command: Command
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Job

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}