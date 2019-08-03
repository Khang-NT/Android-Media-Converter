package com.github.khangnt.mcp.db.job

import android.annotation.SuppressLint
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.util.Optional
import com.github.khangnt.mcp.util.asOptional
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.Executors.newSingleThreadExecutor

/**
 * Created by Khang NT on 4/3/18.
 * Email: khang.neon.1997@gmail.com
 */

interface JobRepository {
    fun addJob(job: Job): Single<Job>
    fun deleteJob(jobId: Long, ignoreError: Boolean): Completable
    fun deleteFinishedJobs(ignoreError: Boolean): Completable
    fun nextReadyJob(): Single<Optional<Job>>
    fun nextPendingJob(): Single<Optional<Job>>
    fun updateJob(job: Job, ignoreError: Boolean): Completable
    fun getCompletedJobs(): Flowable<List<Job>>
    fun getIncompleteJobs(): Flowable<List<Job>>
    fun getJobsByStatus(@JobStatus vararg status: Int): Single<List<Job>>
}

class DefaultJobRepository(private val jobDao: JobDao) : JobRepository {
    private val singleThreadScheduler: Scheduler = Schedulers.from(newSingleThreadExecutor {
        Thread(it, "JobDatabaseThread")
    })

    private val completedJobListChangeEvent = BehaviorSubject.createDefault(Unit)

    override fun addJob(job: Job): Single<Job> = Single.fromCallable { jobDao.insertJob(job) }
            .map { job.copy(id = it) }
            .doAfterSuccess { completedJobListChangeIf(it.isDone()) }
            .subscribeOn(singleThreadScheduler)

    override fun deleteJob(jobId: Long, ignoreError: Boolean): Completable =
            Completable.fromAction { jobDao.deleteJob(jobId) }
                    .doOnComplete { completedJobListChangeIf(true) }
                    .run { if (ignoreError) onErrorComplete() else this }
                    .subscribeOn(singleThreadScheduler)

    override fun deleteFinishedJobs(ignoreError: Boolean): Completable =
            Completable.fromAction { jobDao.deleteFinishedJobs() }
                    .doOnComplete { completedJobListChangeIf(true) }
                    .run { if (ignoreError) onErrorComplete() else this }
                    .subscribeOn(singleThreadScheduler)

    override fun nextReadyJob(): Single<Optional<Job>> = jobDao.getReadyJob()
            .map { it.asOptional() }
            .onErrorReturn { Optional.absent() }
            .subscribeOn(singleThreadScheduler)

    override fun nextPendingJob(): Single<Optional<Job>> = jobDao.getPendingJob()
            .map { it.asOptional() }
            .onErrorReturn { Optional.absent() }
            .subscribeOn(singleThreadScheduler)

    override fun updateJob(job: Job, ignoreError: Boolean): Completable =
            Completable.fromAction { jobDao.updateJob(job) }
                    .doOnComplete { completedJobListChangeIf(job.isDone()) }
                    .run { if (ignoreError) onErrorComplete() else this }
                    .subscribeOn(singleThreadScheduler)

    override fun getCompletedJobs(): Flowable<List<Job>> = completedJobListChangeEvent
            .toFlowable(BackpressureStrategy.LATEST)
            .map { jobDao.getCompletedJobs() }
            .subscribeOn(singleThreadScheduler)

    override fun getIncompleteJobs(): Flowable<List<Job>> = jobDao.getIncompleteJobs()
            .subscribeOn(singleThreadScheduler)

    @SuppressLint("WrongConstant")
    override fun getJobsByStatus(vararg status: Int): Single<List<Job>> =
            jobDao.getJobByStatus(*status)
                    .subscribeOn(singleThreadScheduler)

    private fun completedJobListChangeIf(condition: Boolean) {
        if (condition) {
            completedJobListChangeEvent.onNext(Unit)
        }
    }

}