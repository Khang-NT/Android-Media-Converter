package com.github.khangnt.mcp.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.support.annotation.MainThread
import android.support.v4.app.NotificationCompat
import com.github.khangnt.mcp.BuildConfig
import com.github.khangnt.mcp.CONVERTER_NOTIFICATION_ID
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.misc.RunningJobStatus
import com.github.khangnt.mcp.notification.NotificationHelper
import com.github.khangnt.mcp.ui.EXTRA_PENDING_INTENT
import com.github.khangnt.mcp.ui.PermissionTransparentActivity
import com.github.khangnt.mcp.util.hasWriteStoragePermission
import com.github.khangnt.mcp.util.toJsonOrNull
import com.github.khangnt.mcp.util.toMapString
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit


private const val NOTIFICATION_UPDATE_INTERVAL = 500L
private const val SERVICE_WAKE_LOCK_TAG = "converterService:wakeLock"

const val ACTION_ADD_JOB = "${BuildConfig.APPLICATION_ID}.action.add_job"
const val ACTION_CANCEL_JOB = "${BuildConfig.APPLICATION_ID}.action.cancel_job"

const val EXTRA_JOB_ID = "ConverterService:JobId"
const val EXTRA_JOB_TITLE = "ConverterService:JobTitle"
const val EXTRA_JOB_CMD_INPUT_URIS = "ConverterService:JobCmdInputUris"
const val EXTRA_JOB_CMD_OUTPUT_URI = "ConverterService:JobCmdOutputUri"
const val EXTRA_JOB_CMD_OUTPUT_FMT = "ConverterService:JobCmdOutputFmt"
const val EXTRA_JOB_CMD_ARGS = "ConverterService:JobCmdOutputArgs"
const val EXTRA_JOB_CMD_ENV_VARS_JSON = "ConverterService:JobCmdOutputEnvVars"


private const val ACTION_START_FOREGROUND = "${BuildConfig.APPLICATION_ID}.action.startForeground"
private const val ACTION_STOP_FOREGROUND = "${BuildConfig.APPLICATION_ID}.action.stopForeground"


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
            id = 0,
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
        fun startForeground(appContext: Context) {
            appContext.startService(Intent(appContext, ConverterService::class.java)
                    .setAction(ACTION_START_FOREGROUND))
        }

        fun stopForeground(appContext: Context) {
            appContext.startService(Intent(appContext, ConverterService::class.java)
                    .setAction(ACTION_STOP_FOREGROUND))
        }
    }

    private var pendingAddJobIntentCode: Int = 100

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var inForeground = false
    private var notificationUpdateDisposable: Disposable? = null

    private lateinit var serviceWakeLock: PowerManager.WakeLock

    private val jobWorkerManager by lazy { SingletonInstances.getJobWorkerManager() }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        notificationHelper = NotificationHelper(this)
        notificationBuilder = notificationHelper.createConverterNotification()

        // Show running status in notification
        notificationUpdateDisposable = RunningJobStatus.observeRunningJobStatus()
                .distinctUntilChanged()
                .throttleLast(NOTIFICATION_UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateNotificationIfNeeded, Timber::d)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        serviceWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SERVICE_WAKE_LOCK_TAG)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        goToForeground(force = true)
        if (intent == null) {
            jobWorkerManager.maybeLaunchWorker()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_START_FOREGROUND -> Unit // already on foreground
            ACTION_STOP_FOREGROUND -> stopForeground()

        /****************************** CALLED BY EXTERNAL APP ******************************/
            ACTION_ADD_JOB -> {
                if (!hasWriteStoragePermission(this)) {
                    if (!hasWriteStoragePermission(this)) {
                        // don't add job without write external storage permission granted
                        val pendingIntent = PendingIntent.getService(this,
                                pendingAddJobIntentCode++, intent.cloneFilter().putExtras(intent),
                                PendingIntent.FLAG_ONE_SHOT)
                        startActivity(Intent(this, PermissionTransparentActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .putExtra(EXTRA_PENDING_INTENT, pendingIntent))
                    } else {
                        intent.extras?.toJob()?.let { jobWorkerManager.addJob(it) }
                                ?: jobWorkerManager.maybeLaunchWorker()
                    }
                }
            }
            ACTION_CANCEL_JOB -> {
                val jobId = intent.extras?.getLong(EXTRA_JOB_ID, 0) ?: 0
                if (jobId > 0) {
                    jobWorkerManager.cancelJob(jobId)
                } else {
                    jobWorkerManager.maybeLaunchWorker()
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground()
        notificationUpdateDisposable?.dispose()
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

}
