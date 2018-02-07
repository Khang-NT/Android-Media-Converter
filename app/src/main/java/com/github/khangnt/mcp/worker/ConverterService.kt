package com.github.khangnt.mcp.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.net.Uri
import android.os.*
import android.support.annotation.MainThread
import android.support.v4.app.NotificationCompat
import com.github.khangnt.mcp.*
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.job.Command
import com.github.khangnt.mcp.job.Job
import com.github.khangnt.mcp.job.JobManager
import com.github.khangnt.mcp.notification.NotificationHelper
import com.github.khangnt.mcp.ui.EXTRA_PENDING_INTENT
import com.github.khangnt.mcp.ui.PermissionTransparentActivity
import com.github.khangnt.mcp.util.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.json.JSONObject
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

private const val SERVICE_WAKE_LOCK_TAG = "ConverterServiceWakeLock"

const val ACTION_ADD_JOB = "${BuildConfig.APPLICATION_ID}.action.add_job"
const val ACTION_CANCEL_JOB = "${BuildConfig.APPLICATION_ID}.action.cancel_job"

const val EXTRA_JOB_ID = "ConverterService:JobId"
const val EXTRA_JOB_TITLE = "ConverterService:JobTitle"
const val EXTRA_JOB_CMD_INPUT_URIS = "ConverterService:JobCmdInputUris"
const val EXTRA_JOB_CMD_OUTPUT_URI = "ConverterService:JobCmdOutputUri"
const val EXTRA_JOB_CMD_OUTPUT_FMT = "ConverterService:JobCmdOutputFmt"
const val EXTRA_JOB_CMD_ARGS = "ConverterService:JobCmdOutputArgs"
const val EXTRA_JOB_CMD_ENV_VARS_JSON = "ConverterService:JobCmdOutputEnvVars"

const val ACTION_JOB_STATUS_CHANGED = "${BuildConfig.APPLICATION_ID}.action.job_status_changed"
const val EXTRA_JOB_STATUS = "ConverterService:JobStatus"
const val EXTRA_JOB_OUTPUT = "ConverterService:JobOutput"


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
    return Job(
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
    companion object {
        fun cancelJob(context: Context, jobId: Long) {
            val deleteJobIntent = Intent(context, ConverterService::class.java)
            deleteJobIntent.action = ACTION_CANCEL_JOB
            deleteJobIntent.putExtra(EXTRA_JOB_ID, jobId)
            context.startService(deleteJobIntent)
        }

        fun newJob(
                context: Context,
                title: String,
                inputs: List<String>,
                args: String,
                outputUri: String,
                outputFormat: String,
                environmentVars: Map<String, String> = emptyMap()
        ) {
            val newJobIntent = Intent(context, ConverterService::class.java)
                    .setAction(ACTION_ADD_JOB)
                    .putExtra(EXTRA_JOB_TITLE, title)
                    .putStringArrayListExtra(EXTRA_JOB_CMD_INPUT_URIS, ArrayList(inputs))
                    .putExtra(EXTRA_JOB_CMD_ARGS, args)
                    .putExtra(EXTRA_JOB_CMD_OUTPUT_URI, outputUri)
                    .putExtra(EXTRA_JOB_CMD_OUTPUT_FMT, outputFormat)
                    .putExtra(EXTRA_JOB_CMD_ENV_VARS_JSON, JSONObject(environmentVars).toString())
            context.startService(newJobIntent)
        }
    }

    private var pendingAddJobIntentCode: Int = 100

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

    private lateinit var serviceWakeLock: PowerManager.WakeLock

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
                .getLiveLogObservable().distinctUntilChanged()
                .throttleLast(NOTIFICATION_UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateNotificationIfNeeded, Timber::d)

        jobHandler = JobHandler(jobHandlerThread.looper)
        jobHandler.sendEmptyMessage(INIT_MESSAGE)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        serviceWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SERVICE_WAKE_LOCK_TAG)
    }

    fun getJobManager(): JobManager = jobManager

    override fun onStartCommand(intentNullable: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // go to foreground immediately, maybe service started with Context#startForegroundService
            // if no job to run, service will stop foreground soon
            goToForeground(force = true)
            jobHandler.sendEmptyMessage(LOOP_MESSAGE)
        }
        intentNullable?.let { intent ->
            val shouldPostponeStopMessage = when (intent.action) {
                ACTION_ADD_JOB -> {
                    if (!hasWriteStoragePermission(this)) {
                        // don't add job without write external storage permission granted
                        val pendingIntent = PendingIntent.getService(this,
                                pendingAddJobIntentCode++, intent.cloneFilter().putExtras(intent),
                                PendingIntent.FLAG_ONE_SHOT)
                        startActivity(Intent(this, PermissionTransparentActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(EXTRA_PENDING_INTENT, pendingIntent))
                        false
                    } else {
                        val msg = Message().apply {
                            what = ADD_JOB_MESSAGE
                            data = intent.extras
                        }
                        mainHandler.sendMessage(msg)

                        true
                    }
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
        stopForeground()
        jobHandlerThread.quit()
        jobHandler.forceShutdown()
        notificationUpdateDisposable?.dispose()
    }

    private fun onJobStatusChanged(job: Job) {
        if (job.status == JobStatus.COMPLETED) {
            SingletonInstances.getSharedPrefs().successJobsCount += 1
        }

        val intent = Intent(ACTION_JOB_STATUS_CHANGED)
        intent.putExtra(EXTRA_JOB_ID, job.id)
        intent.putExtra(EXTRA_JOB_STATUS, job.status)
        intent.putExtra(EXTRA_JOB_OUTPUT, job.command.output)
        sendBroadcast(intent)
    }

    @MainThread
    private fun updateNotificationIfNeeded(outputSize: String) {
        if (inForeground) {
            // only show notification in foreground
            if (outputSize.isNotBlank()) {
                notificationBuilder.setContentText(
                        getString(R.string.converter_service_running_with_speed, outputSize)
                )
            } else {
                notificationBuilder.setContentText(getString(R.string.converter_service_running))
            }
            notificationHelper.notify(CONVERTER_NOTIFICATION_ID, notificationBuilder)
        }
    }

    @SuppressLint("WakelockTimeout")
    @MainThread
    private fun goToForeground(force: Boolean = false) {
        if (!inForeground || force) {
            inForeground = true
            startForeground(CONVERTER_NOTIFICATION_ID, notificationBuilder.build())
        }
        if (!serviceWakeLock.isHeld) {
            serviceWakeLock.acquire()
        }
    }

    @MainThread
    private fun stopForeground() {
        if (inForeground) {
            inForeground = false
            stopForeground(true)
        }
        if (serviceWakeLock.isHeld) {
            serviceWakeLock.release()
        }
    }

    private fun runOnMainThread(action: () -> Unit) {
        mainHandler.post(action)
    }

    private fun mainHandlerCallback(): Handler.Callback = Handler.Callback { message ->
        when (message.what) {
            ADD_JOB_MESSAGE, CANCEL_JOB_MESSAGE -> {
                // pass it to Job Handler
                jobHandler.resetFreeTimes()
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

        private var runningJob: Job? = null
        private var preparingJob: Job? = null
        private var workerThread: JobWorkerThread? = null
        private var prepareThread: JobPrepareThread? = null

        private var freeTime = 0

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                INIT_MESSAGE -> {
                    val previousRunning = jobManager.getJob(JobStatus.RUNNING).blockingFirst()
                    // mark previous running job as ready so it can restart
                    previousRunning.forEach { job ->
                        jobManager.updateJobStatus(
                                job = job,
                                status = JobStatus.READY,
                                statusDetail = "Ready to restart."
                        ).apply { onJobStatusChanged(this) }
                    }

                    val previousPreparing = jobManager.getJob(JobStatus.PREPARING).blockingFirst()
                    // mark previous preparing job as pending, so it can restart prepare process
                    previousPreparing.forEach { job ->
                        jobManager.updateJobStatus(
                                job = job,
                                status = JobStatus.PENDING,
                                statusDetail = "Ready to prepare again."
                        ).apply { onJobStatusChanged(this) }
                    }

                    loop()
                }
                LOOP_MESSAGE -> {
                    if (workerThread?.isAlive == true
                            && prepareThread?.isAlive == true) {
                        // all busy
                        loop()
                        return
                    }
                    var isFree = true

                    if (workerThread?.isAlive != true) {
                        val nextJobToRun = jobManager.nextReadyJob()
                        if (nextJobToRun != null) {
                            isFree = false

                            runningJob = jobManager.updateJobStatus(nextJobToRun, JobStatus.RUNNING)
                            onJobStatusChanged(runningJob!!)

                            workerThread = JobWorkerThread(
                                    appContext = applicationContext,
                                    job = runningJob!!,
                                    jobManager = jobManager,
                                    onCompleteListener = { job ->
                                        onJobStatusChanged(job)
                                        loop()
                                    },
                                    onErrorListener = { job, _ ->
                                        onJobStatusChanged(job)
                                        loop()
                                    }
                            ).apply {
                                start() // start worker thread
                            }
                        }
                    } else {
                        isFree = false
                    }

                    if (prepareThread?.isAlive != true) {
                        val nextJobToPrepare = jobManager.nextPendingJob()
                        if (nextJobToPrepare != null) {
                            isFree = false

                            preparingJob = jobManager.updateJobStatus(nextJobToPrepare, JobStatus.PREPARING)
                            onJobStatusChanged(preparingJob!!)

                            prepareThread = JobPrepareThread(
                                    appContext = applicationContext,
                                    job = preparingJob!!,
                                    jobManager = jobManager,
                                    onCompleteListener = { job ->
                                        onJobStatusChanged(job)
                                        loop()
                                    },
                                    onErrorListener = { job, _ ->
                                        onJobStatusChanged(job)
                                        loop()
                                    }
                            ).apply {
                                start() // start prepare thread
                            }
                        }
                    } else {
                        isFree = false
                    }

                    if (!isFree) {
                        resetFreeTimes()
                        if (!inForeground) {
                            runOnMainThread { goToForeground() }
                        }
                        loop()
                    } else {
                        if (inForeground) {
                            runOnMainThread { stopForeground() }
                        }

                        freeTime += 1
                        if (freeTime >= JOB_HANDLER_MAX_FREE_TIME && !binding) {
                            catchAll {
                                // clean up temp folder before stop
                                makeWorkingPaths(this@ConverterService).getListJobTempDir()
                                        .forEach { it.deleteRecursiveIgnoreError() }
                            }
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
                        with(Uri.parse(job.command.output)) {
                            if (scheme?.startsWith("content") == true) {
                                catchAll {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        contentResolver.takePersistableUriPermission(this,
                                                FLAG_GRANT_READ_URI_PERMISSION or
                                                        FLAG_GRANT_WRITE_URI_PERMISSION)
                                    }
                                }
                            }
                        }
                        jobManager.addJob(job)
                        loop()
                    }
                }
                CANCEL_JOB_MESSAGE -> {
                    msg.data?.getLong(EXTRA_JOB_ID, -1)?.let { id ->
                        if (id >= 0) {
                            when (id) {
                                runningJob?.id -> workerThread?.interrupt()
                                preparingJob?.id -> prepareThread?.interrupt()
                                else -> jobManager.deleteJob(id)
                            }
                            catchAll {
                                // clean up temp and log files
                                makeWorkingPaths(this@ConverterService).apply {
                                    getTempDirForJob(id).deleteRecursiveIgnoreError()
                                    getLogFileOfJob(id).delete()
                                }
                            }
                            loop()
                        }
                    }
                }
            }
        }

        fun forceShutdown() {
            workerThread?.interrupt()
            prepareThread?.interrupt()
        }

        fun resetFreeTimes() {
            freeTime = 0
        }

        private fun loop() {
            sendEmptyMessageDelayed(LOOP_MESSAGE, JOB_HANDLER_LOOP_INTERVAL)
        }
    }
}

class ConverterServiceBinder(val service: ConverterService) : Binder()
