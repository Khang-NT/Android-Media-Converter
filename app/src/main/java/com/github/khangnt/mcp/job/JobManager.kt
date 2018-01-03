package com.github.khangnt.mcp.job

import com.github.khangnt.mcp.annotation.JobStatus
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Created by Khang NT on 12/30/17.
 * Email: khang.neon.1997@gmail.com
 */

interface JobManager {
    fun recordWriting(byteCount: Int)
    fun getWritingSpeed(): Observable<Int>

    fun addJob(job: Job): Job
    fun deleteJob(job: Job)
    fun nextJobToRun(): Job?
    fun updateJobStatus(job: Job, @JobStatus status: Int, statusDetail: String? = null): Job

    fun getJob(@JobStatus vararg jobStatus: Int): Flowable<List<Job>>
}

