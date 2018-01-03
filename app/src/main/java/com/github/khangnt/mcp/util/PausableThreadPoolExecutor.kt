package com.github.khangnt.mcp.util

import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock

class PausableThreadPoolExecutor(
        corePoolSize: Int,
        maximumPoolSize: Int,
        keepAliveTime: Long,
        unit: TimeUnit,
        workQueue: BlockingQueue<Runnable>,
        threadFactory: ThreadFactory
) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory) {

    companion object {
        fun newSingleThreadExecutor(threadFactory: ThreadFactory): PausableThreadPoolExecutor {
            return PausableThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    LinkedBlockingQueue(),
                    threadFactory)
        }
    }


    private var isPaused: Boolean = false
    private val pauseLock = ReentrantLock()
    private val unpaused = pauseLock.newCondition()


    override fun beforeExecute(t: Thread, r: Runnable) {
        super.beforeExecute(t, r)
        pauseLock.lock()
        try {
            while (isPaused) unpaused.await()
        } catch (ie: InterruptedException) {
            t.interrupt()
        } finally {
            pauseLock.unlock()
        }
        Executors.newSingleThreadExecutor()
    }

    fun pause(): PausableThreadPoolExecutor {
        pauseLock.lock()
        try {
            isPaused = true
        } finally {
            pauseLock.unlock()
        }
        return this
    }

    fun resume(): PausableThreadPoolExecutor {
        pauseLock.lock()
        try {
            isPaused = false
            unpaused.signalAll()
        } finally {
            pauseLock.unlock()
        }
        return this
    }

    override fun finalize() {
        shutdown()
        super.finalize()
    }
}