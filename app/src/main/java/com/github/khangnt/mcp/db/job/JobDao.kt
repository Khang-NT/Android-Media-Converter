package com.github.khangnt.mcp.db.job

import android.arch.persistence.room.*
import com.github.khangnt.mcp.annotation.JobStatus.*
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by Khang NT on 4/3/18.
 * Email: khang.neon.1997@gmail.com
 */

@Dao
interface JobDao {
    @Query("""SELECT * FROM jobs_v2 WHERE status == $COMPLETED OR status == $FAILED
        ORDER BY _id DESC""")
    fun getCompletedJobs(): List<Job>

    @Query("""SELECT * FROM jobs_v2 WHERE status != $COMPLETED AND status != $FAILED
        ORDER BY status DESC, _id ASC""")
    fun getIncompleteJobs(): Flowable<List<Job>>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertJob(job: Job): Long

    @Update
    fun updateJob(job: Job)

    @Query("DELETE FROM jobs_v2 WHERE _id == :jobId")
    fun deleteJob(jobId: Long)

    @Query("DELETE FROM jobs_v2 WHERE status == $COMPLETED OR status == $FAILED")
    fun deleteFinishedJobs()

    @Query("SELECT * FROM jobs_v2 WHERE status == $READY ORDER BY _id ASC LIMIT 1")
    fun getReadyJob(): Single<Job>

    @Query("SELECT * FROM jobs_v2 WHERE status == $PENDING ORDER BY _id ASC LIMIT 1")
    fun getPendingJob(): Single<Job>

    @Query("SELECT * FROM jobs_v2 WHERE status IN (:status)")
    fun getJobByStatus(vararg status: Int): Single<List<Job>>
}
