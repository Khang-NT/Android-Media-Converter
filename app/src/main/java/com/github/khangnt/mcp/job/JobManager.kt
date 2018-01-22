package com.github.khangnt.mcp.job

import com.github.khangnt.mcp.annotation.JobStatus
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Created by Khang NT on 12/30/17.
 * Email: khang.neon.1997@gmail.com
 */

interface JobManager {
    fun recordLiveLog(size: String)
    fun getLiveLogObservable(): Observable<String>

    fun addJob(job: Job): Job
    fun deleteJob(job: Job)
    fun deleteJob(jobId: Long)
    fun nextReadyJob(): Job?
    fun nextPendingJob(): Job?
    fun updateJobStatus(job: Job, @JobStatus status: Int, statusDetail: String? = null): Job

    fun getJob(@JobStatus vararg jobStatus: Int): Flowable<MutableList<Job>>
}

