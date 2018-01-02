package com.github.khangnt.mcp.job

import com.github.khangnt.mcp.annotation.JobStatus
import io.reactivex.Observable

/**
 * Created by Khang NT on 12/30/17.
 * Email: khang.neon.1997@gmail.com
 */

interface JobManager {
    fun recordWriting(byteCount: Long)
    fun getWritingSpeed(): Observable<Long>

    fun addJob(job: Job)
    fun cancelJob(job: Job)
    fun nextJobIfAny(): Job?
    fun updateJobStatus(job: Job, @JobStatus status: Long, statusDetail: String? = null)

    fun getRunningJob(@JobStatus vararg jobStatus: Long): Observable<List<Job>>
}

