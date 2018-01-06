package com.github.khangnt.mcp.job

import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.annotation.JobStatus.PENDING
import com.github.khangnt.mcp.annotation.JobStatus.RUNNING

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

val jobComparator: Comparator<Job> = Comparator { job1, job2 ->
    if (job1.status == job2.status) {
        return@Comparator job2.id.compareTo(job1.id)
    }

    if (job1.status == RUNNING) {
        return@Comparator -1
    }

    if (job2.status == RUNNING) {
        return@Comparator 1
    }

    if (job1.status == PENDING) {
        return@Comparator -1
    }

    if (job2.status == PENDING) {
        return@Comparator 1
    }

    return@Comparator job2.id.compareTo(job1.id)
}