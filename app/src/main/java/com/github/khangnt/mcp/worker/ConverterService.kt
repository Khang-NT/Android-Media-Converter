package com.github.khangnt.mcp.worker

import android.app.Service
import android.content.Intent
import android.os.*
import android.support.annotation.MainThread
import android.support.v4.app.NotificationCompat
import com.github.khangnt.mcp.*
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.job.Command
import com.github.khangnt.mcp.job.Job
import com.github.khangnt.mcp.job.JobManager
import com.github.khangnt.mcp.notification.NotificationHelper
import com.github.khangnt.mcp.util.toConverterSpeed
import com.github.khangnt.mcp.util.toJsonOrNull
import com.github.khangnt.mcp.util.toMapString
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by Khang NT on 1/3/18.
 * Email: khang.neon.1997@gmail.com
 */

// Job handler
private const val INIT_MESSAGE = 0
private const val LOOP_MESSAGE = 1

// both job handler and main handler
private const val ADD_JOB_MESSAGE = 2
private const val CANCEL_JOB_MESSAGE = 3

// main handler
private const val STOP_SERVICE_MESSAGE = 4

private const val JOB_HANDLER_LOOP_INTERVAL = 500L
private const val JOB_HANDLER_MAX_FREE_TIME = 10_000 / JOB_HANDLER_LOOP_INTERVAL

private const val NOTIFICATION_UPDATE_INTERVAL = 500L

const val ACTION_ADD_JOB = "${BuildConfig.APPLICATION_ID}.action.add_job"
const val ACTION_CANCEL_JOB = "${BuildConfig.APPLICATION_ID}.action.cancel_job"

const val EXTRA_JOB_ID = "ConverterService:JobId"
const val EXTRA_JOB_TITLE = "ConverterService:JobTitle"
const val EXTRA_JOB_CMD_INPUT_URIS = "ConverterService:JobCmdInputUris"
const val EXTRA_JOB_CMD_OUTPUT_URI = "ConverterService:JobCmdOutputUri"
const val EXTRA_JOB_CMD_OUTPUT_FMT = "ConverterService:JobCmdOutputFmt"
const val EXTRA_JOB_CMD_ARGS = "ConverterService:JobCmdOutputArgs"
const val EXTRA_JOB_CMD_ENV_VARS_JSON = "ConverterService:JobCmdOutputEnvVars"

private fun Bundle.toJob(): Job? {
    val title: String = getString(EXTRA_JOB_TITLE, "Untitled")
    val inputUris: List<String> = getStringArrayList(EXTRA_JOB_CMD_INPUT_URIS) ?: emptyList()
    val outputUri: String = getString(EXTRA_JOB_CMD_OUTPUT_URI) ?: ""
    val outputFormat: String = getString(EXTRA_JOB_CMD_OUTPUT_FMT) ?: ""
    val args: String = getString(EXTRA_JOB_CMD_ARGS) ?: ""
    val environmentVarsJsonString: String = getString(EXTRA_JOB_CMD_ENV_VARS_JSON) ?: "{}"
    val environmentVarsJson = environmentVarsJsonString.toJsonOrNull()
    // validate
    if (inputUris.isEmpty() ||
            inputUris.any { it.isBlank() } ||
            outputUri.isBlank() ||
            outputFormat.isBlank() ||
            environmentVarsJson === null) {
        Timber.d("Invalid Job in bundle: ${this}")
        return null
    }
    return Job (
            id = ID_UNSET,
            title = title,
            status = JobStatus.PENDING,
            statusDetail = null,
            command = Command(
                    inputs = inputUris,
                    output = outputUri,
                    outputFormat = outputFormat,
                    args = args,
                    environmentVars = environmentVarsJson.toMapString()
            )
    )
}

class ConverterService : Service() {

    private val binder = ConverterServiceBinder(this)
    private val jobManager: JobManager = SingletonInstances.getJobManager()

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var binding = false
    private var inForeground = false
    private var notificationUpdateDisposable: Disposable? = null

    private val mainHandler = Handler(mainHandlerCallback())
    private val jobHandlerThread: HandlerThread
    private lateinit var jobHandler: JobHandler

    init {
        jobHandlerThread = HandlerThread("JobHandlerThread")
        jobHandlerThread.start() // start handler thread first, use it onCreate
    }

    override fun onBind(intent: Intent): IBinder = binder.also { binding = true }

    override fun onUnbind(intent: Intent?): Boolean = false.also { binding = false }

    override fun onCreate() {
        super.onCreate()

        notificationHelper = NotificationHelper(this)
        notificationBuilder = notificationHelper.createConverterNotification()

        // original source emit 4 items per second
        // limit update 2 times per second on main thread
        notificationUpdateDisposable = jobManager
                .getWritingSpeed().distinctUntilChanged()
                .throttleLast(NOTIFICATION_UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateNotificationIfNeeded, Timber::d)

        jobHandler = JobHandler(jobHandlerThread.looper)
        jobHandler.sendEmptyMessage(INIT_MESSAGE)
    }

    override fun onStartCommand(intentNullable: Intent?, flags: Int, startId: Int): Int {
        intentNullable?.let { intent ->
            val shouldPostponeStopMessage = when(intent.action) {
                ACTION_ADD_JOB -> {
                    val msg = Message().apply {
                        what = ADD_JOB_MESSAGE
                        data = intent.extras
                    }
                    mainHandler.sendMessage(msg)

                    true
                }
                ACTION_CANCEL_JOB -> {
                    val msg = Message().apply {
                        what = CANCEL_JOB_MESSAGE
                        data = intent.extras
                    }
                    mainHandler.sendMessage(msg)

                    true
                }
                else -> false
            }
            if (shouldPostponeStopMessage) {
                mainHandler.removeMessages(STOP_SERVICE_MESSAGE)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        jobHandlerThread.quit()
        jobHandler.forceShutdown()
        notificationUpdateDisposable?.dispose()
    }

    @MainThread
    private fun updateNotificationIfNeeded(speed: Int) {
        if (inForeground) {
            // only show notification in foreground
            if (speed > 0) {
                notificationBuilder.setContentText(
                        getString(R.string.converter_service_running_with_speed,
                                speed.toConverterSpeed())
                )
            } else {
                notificationBuilder.setContentText(getString(R.string.converter_service_running))
            }
            notificationHelper.notify(CONVERTER_NOTIFICATION_ID, notificationBuilder)
        }
    }

    @MainThread
    private fun goToForeground() {
        if (!inForeground) {
            inForeground = true
            startForeground(CONVERTER_NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    @MainThread
    private fun stopForeground() {
        if (inForeground) {
            inForeground = false
            stopForeground(true)
        }
    }

    private fun runOnMainThread(action: () -> Unit) {
        mainHandler.post(action)
    }

    private fun mainHandlerCallback(): Handler.Callback = Handler.Callback { message ->
        when (message.what) {
            ADD_JOB_MESSAGE, CANCEL_JOB_MESSAGE -> {
                // pass it to Job Handler
                jobHandler.resetNoJobTimes()
                jobHandler.sendMessage(Message().apply { copyFrom(message) })
                // cancel any stop Message
                mainHandler.removeMessages(STOP_SERVICE_MESSAGE)
            }
            STOP_SERVICE_MESSAGE -> {
                stopForeground()
                notificationHelper.cancel(CONVERTER_NOTIFICATION_ID) // ensure remove notification
                stopSelf()
            }
        }

        return@Callback true
    }

    inner class JobHandler(looper: Looper) : Handler(looper) {

        private var currentJob: Job? = null
        private var workerThread: JobWorkerThread? = null
        private var freeTime = 0

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                INIT_MESSAGE -> {
                    val previousRunning = jobManager.getJob(JobStatus.RUNNING).blockingFirst()
                    // these jobs were running last time, but service restart -> they should fail
                    // mark them as "Cancelled because service restart"
                    previousRunning.forEach { job ->
                        jobManager.updateJobStatus(
                                job = job,
                                status = JobStatus.FAILED,
                                statusDetail = "Cancelled because service restart"
                        )
                    }

                    loop()
                }
                LOOP_MESSAGE -> {
                    if (workerThread?.isAlive == true) {
                        // current thread still running
                        loop()
                        return
                    }

                    // start working new job

                    val nextJob = jobManager.nextJobToRun()
                    if (nextJob != null) {
                        resetNoJobTimes()
                        runOnMainThread { goToForeground() }

                        currentJob = jobManager.updateJobStatus(nextJob, JobStatus.RUNNING)
                        workerThread = JobWorkerThread(
                                appContext = applicationContext,
                                job = currentJob!!,
                                jobManager = jobManager,
                                onCompleteListener = {
                                    // maybe show notification
                                    loop()
                                },
                                onErrorListener = { _, _ ->
                                    // maybe show notification
                                    loop()
                                }
                        ).apply {
                            this.start() // start worker thread
                        }
                    } else {
                        // no job found
                        if (inForeground) {
                            runOnMainThread { stopForeground() }
                        }

                        freeTime += 1
                        if (freeTime >= JOB_HANDLER_MAX_FREE_TIME && !binding) {
                            // request stop
                            mainHandler.sendEmptyMessage(STOP_SERVICE_MESSAGE)
                        } else {
                            // continue loop
                            loop()
                        }
                    }
                }
                ADD_JOB_MESSAGE -> {
                    msg.data?.toJob()?.let { job ->
                        jobManager.addJob(job)
                        loop()
                    }
                }
                CANCEL_JOB_MESSAGE -> {
                    msg.data?.getLong(EXTRA_JOB_ID, -1)?.let { id ->
                        if (id >= 0) {
                            if (id == currentJob?.id) {
                                // cancel running job
                                workerThread?.interrupt()
                            } else {
                                jobManager.deleteJob(id)
                            }
                            loop()
                        }
                    }
                }
            }
        }

        fun forceShutdown() {
            workerThread?.interrupt()
        }

        fun resetNoJobTimes() {
            freeTime = 0
        }

        private fun loop() {
            sendEmptyMessageDelayed(LOOP_MESSAGE, JOB_HANDLER_LOOP_INTERVAL)
        }
    }
}

class ConverterServiceBinder(val service: ConverterService) : Binder()
