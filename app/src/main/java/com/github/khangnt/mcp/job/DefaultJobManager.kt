package com.github.khangnt.mcp.job

import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.db.JobDb
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.ignoreError
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Khang NT on 1/3/18.
 * Email: khang.neon.1997@gmail.com
 */

class DefaultJobManager(private val jobDb: JobDb) : JobManager {
    private val lock = Any()

    private var loadJobToMemory = false
    private val mapJobList = mapOf(
            JobStatus.RUNNING to mutableListOf<Job>(),
            JobStatus.PENDING to mutableListOf(),
            JobStatus.COMPLETED to mutableListOf(),
            JobStatus.FAILED to mutableListOf()
    )
    private val mapSubject = mapOf(
            JobStatus.RUNNING to BehaviorSubject.create<List<Job>>(),
            JobStatus.PENDING to BehaviorSubject.create<List<Job>>(),
            JobStatus.COMPLETED to BehaviorSubject.create<List<Job>>(),
            JobStatus.FAILED to BehaviorSubject.create<List<Job>>()
    )

    private val writingSpeedInOneSecond = intArrayOf(0, 0, 0, 0)
    private val writingSpeedSubject = BehaviorSubject.create<Int>()
    private var pos: Int = 0
    private var timerRunning = false

    private fun writingSpeedOneSecond(): Int {
        return writingSpeedInOneSecond[0] + writingSpeedInOneSecond[1] +
                writingSpeedInOneSecond[2] + writingSpeedInOneSecond[3];
    }

    private fun startTimerIfNeeded() {
        synchronized(writingSpeedInOneSecond) {
            if (!timerRunning) {
                timerRunning = true
                Observable.interval(0, 250, TimeUnit.MILLISECONDS)
                        .map {
                            val speed = writingSpeedOneSecond()
                            writingSpeedSubject.onNext(speed)
                            pos = it.toInt() % 4
                            writingSpeedInOneSecond[pos] = 0
                            return@map speed
                        }
                        .filter { it > 0 }
                        .timeout(15, TimeUnit.SECONDS, Observable.empty())
                        .ignoreError(printLog = true)
                        .doFinally { timerRunning = false }
                        .subscribe()
            }
        }
    }

    override fun recordWriting(byteCount: Int) {
        startTimerIfNeeded()
        writingSpeedInOneSecond[pos] += byteCount
    }

    override fun getWritingSpeed(): Observable<Int> = writingSpeedSubject

    override fun addJob(job: Job): Job {
        loadJobToMemoryIfNeeded()
        synchronized(lock) {
            // execute db insert synchronously
            val newJobId = jobDb.insertJob(job)
            val newJob = job.copy(id = newJobId)
            mapJobList[newJob.status]!!.add(newJob)
            notifyJobListChanged(newJob.status)
            return newJob
        }
    }

    override fun deleteJob(job: Job) {
        loadJobToMemoryIfNeeded()
        synchronized(lock) {
            val jobList = mapJobList[job.status]!!
            if (jobList.remove(job)) {
                notifyJobListChanged(job.status)
                runOnDbThread {
                    catchAll(printLog = true) { jobDb.deleteJob(job) }
                }
            }
        }
    }

    override fun deleteJob(jobId: Long) {
        loadJobToMemoryIfNeeded()
        synchronized(lock) {
            var removed = false
            mapJobList.values.forEach { jobList ->
                val listIterator = jobList.listIterator()
                while (listIterator.hasNext()) {
                    if (listIterator.next().id == jobId) {
                        listIterator.remove()
                        removed = true
                    }
                }
            }
            if (removed) {
                runOnDbThread {
                    catchAll { jobDb.deleteJob(jobId) }
                }
            }
        }
    }

    override fun nextJobToRun(): Job? {
        loadJobToMemoryIfNeeded()
        synchronized(lock) {
            val pendingJobs = mapJobList[JobStatus.PENDING]!!
            if (!pendingJobs.isEmpty()) {
                return pendingJobs[0]
            }
        }
        return null
    }

    override fun updateJobStatus(job: Job, status: Int, statusDetail: String?): Job {
        loadJobToMemoryIfNeeded()

        val newJob = job.copy(status = status, statusDetail = statusDetail)
        synchronized(lock) {
            // update memory value first
            mapJobList[job.status]!!.remove(job)
            mapJobList[newJob.status]!!.add(newJob)

            if (job.status != newJob.status) {
                notifyJobListChanged(job.status, newJob.status)
            } else {
                notifyJobListChanged(job.status)
            }
        }

        // update db async
        runOnDbThread {
            catchAll(printLog = true) { jobDb.updateJob(newJob) }
        }
        return newJob
    }

    override fun getJob(vararg jobStatus: Int): Flowable<List<Job>> {
        loadJobToMemoryIfNeeded()

        val subjects = jobStatus.map { mapSubject[it]!!.toFlowable(BackpressureStrategy.LATEST) }
        if (subjects.isEmpty()) {
            return Flowable.empty()
        }
        return Flowable.combineLatest(subjects, { jobListList ->
            val combinedList = mutableListOf<Job>()
            @Suppress("UNCHECKED_CAST")
            jobListList.map { it as List<Job> }.forEach { combinedList.addAll(it) }
            return@combineLatest combinedList
        })
    }

    private fun runOnDbThread(command: () -> Unit) {
        Completable.fromAction(command).subscribeOn(Schedulers.single()).subscribe()
    }

    private fun loadJobToMemoryIfNeeded() {
        synchronized(lock) {
            if (!loadJobToMemory) {
                jobDb.getAllJob().forEach { mapJobList[it.status]!!.add(it) }
                loadJobToMemory = true
                notifyJobListChanged(JobStatus.RUNNING, JobStatus.PENDING, JobStatus.COMPLETED, JobStatus.FAILED)
            }
        }
    }

    private fun notifyJobListChanged(@JobStatus vararg status: Int) {
        status.forEach { mapSubject[it]!!.onNext(mapJobList[it]!!) }
    }

}