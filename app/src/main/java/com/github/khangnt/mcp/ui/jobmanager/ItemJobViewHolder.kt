package com.github.khangnt.mcp.ui.jobmanager

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.StrictMode
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatTextView
import android.util.SparseArray
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.annotation.JobStatus
import com.github.khangnt.mcp.annotation.JobStatus.*
import com.github.khangnt.mcp.job.Job
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.CustomViewHolder
import com.github.khangnt.mcp.ui.common.HasIdModel
import com.github.khangnt.mcp.util.UriUtils
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.worker.ConverterService
import java.io.File
import java.net.URLConnection


/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

data class JobModel(val job: Job) : AdapterModel, HasIdModel {
    override val modelId: Long = job.id
}

private val cacheOutputPath = SparseArray<String>()

class ItemJobViewHolder(itemView: View) : CustomViewHolder<JobModel>(itemView) {

    companion object {
        private fun jobStatusName(jobStatus: Int): String {
            return when (jobStatus) {
                JobStatus.RUNNING -> "RUNNING"
                JobStatus.PENDING -> "PENDING"
                JobStatus.PREPARING -> "PREPARING"
                JobStatus.READY -> "READY"
                JobStatus.FAILED -> "FAILED"
                JobStatus.COMPLETED -> "SUCCESS"
                else -> "$jobStatus"
            }
        }
    }

    private val context = itemView.context

    private val tvFileFmt by lazy { itemView.findViewById<AppCompatTextView>(R.id.tvOutputFormat) }
    private val tvJobTitle by lazy { itemView.findViewById<TextView>(R.id.tvJobTitle) }
    private val tvJobStatus by lazy { itemView.findViewById<TextView>(R.id.tvJobStatus) }
    private val tvJobLocation by lazy { itemView.findViewById<TextView>(R.id.tvJobLocation) }
    private val ivDeleteJob by lazy { itemView.findViewById<ImageView>(R.id.ivCancelJob) }

    private val buttonLayout by lazy { itemView.findViewById<LinearLayout>(R.id.buttonLayout) }
    private val ivShare by lazy { itemView.findViewById<ImageView>(R.id.ivShare) }
    private val ivOpen by lazy { itemView.findViewById<ImageView>(R.id.ivOpen) }
    private val ivOpenFolder by lazy { itemView.findViewById<ImageView>(R.id.ivOpenFolder) }
    private val ivDeleteFile by lazy { itemView.findViewById<ImageView>(R.id.ivDeleteFile) }

    private var currentJob: Job? = null
    private var vmPolicyBackup: StrictMode.VmPolicy? = null

    init {
        ivDeleteJob.setOnClickListener {
            cancelJob(context, currentJob!!, false)
        }
        ivShare.setOnClickListener {
            var outputUri = Uri.parse(currentJob!!.command.output)
            if (outputUri.scheme == ContentResolver.SCHEME_CONTENT) {
                // try to convert it to file scheme
                getPath(currentJob!!)?.let { outputUri = Uri.fromFile(File(it)) }
            }
            val intent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_STREAM, outputUri)
                    .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_file_subject))
            grantWritePermission(outputUri, intent)
            disableStrictMode()
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser)))
            restoreStrictMode()
        }
        ivOpen.setOnClickListener {
            var outputUri = Uri.parse(currentJob!!.command.output)
            if (outputUri.scheme == ContentResolver.SCHEME_CONTENT) {
                // try to convert it to file scheme
                getPath(currentJob!!)?.let { outputUri = Uri.fromFile(File(it)) }
            }

            val mimeType = catchAll {
                URLConnection.guessContentTypeFromName(outputUri.toString())
            } ?: MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(currentJob!!.command.outputFormat)

            val intent = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(outputUri, mimeType)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            grantWritePermission(outputUri, intent)

            disableStrictMode()
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_file_chooser)))
            restoreStrictMode()
        }
        ivOpenFolder.setOnClickListener {
            val path = getPath(currentJob!!)
            if (path === null) {
                Toast.makeText(context, R.string.unknown_file_path, Toast.LENGTH_SHORT).show()
            } else {
                val folder = File(path).parentFile
                // this intent should work with ES Explorer
                val intent = Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.fromFile(folder), "resource/folder")
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                disableStrictMode()
                try {
                    context.startActivity(intent)
                    restoreStrictMode()
                    return@setOnClickListener
                } catch (ignore: ActivityNotFoundException) {
                }

                // otherwise, use mime type "*/*"
                intent.setDataAndType(Uri.fromFile(folder), "*/*")
                context.startActivity(Intent.createChooser(intent,
                        context.getString(R.string.open_folder_chooser, folder.absolutePath)))
                restoreStrictMode()
            }
        }
        ivDeleteFile.setOnClickListener {
            val job = currentJob!!
            val path = getPath(job) ?: ""
            AlertDialog.Builder(context)
                    .setTitle(R.string.dialog_confirm_delete_job_output)
                    .setMessage(context.getString(R.string.dialog_confirm_delete_job_output_mess, path))
                    .setPositiveButton(R.string.action_yes, { _, _ ->
                        cancelJob(context, job, true)
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
        }
    }

    private fun grantWritePermission(outputUri: Uri, intent: Intent) {
        if (outputUri.scheme == ContentResolver.SCHEME_CONTENT) {
            catchAll {
                val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resInfo in resInfoList) {
                    context.grantUriPermission(resInfo.activityInfo.packageName,
                            outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
        }
    }

    private fun disableStrictMode() {
        vmPolicyBackup = StrictMode.getVmPolicy()
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    private fun restoreStrictMode() {
        vmPolicyBackup?.let(StrictMode::setVmPolicy)
    }

    private fun getPath(job: Job): String? {
        var path = cacheOutputPath.get(job.id.toInt())
        if (path === null) {
            path = catchAll { UriUtils.getPathFromUri(context, Uri.parse(job.command.output)) }
            if (path !== null) {
                cacheOutputPath.put(job.id.toInt(), path)
            }
        }
        return path
    }

    @SuppressLint("SetTextI18n")
    override fun bind(model: JobModel, pos: Int) {
        currentJob = model.job
        model.job.apply {
            tvFileFmt.text = command.outputFormat
                    .substring(0, minOf(3, command.outputFormat.length)).toUpperCase()
            ViewCompat.setBackgroundTintList(tvFileFmt, ColorStateList.valueOf(
                    when (status) {
                        RUNNING, PREPARING -> ContextCompat.getColor(context, R.color.teal_500)
                        PENDING, READY -> ContextCompat.getColor(context, R.color.blue_grey_500)
                        COMPLETED -> ContextCompat.getColor(context, R.color.green_600)
                        FAILED -> ContextCompat.getColor(context, R.color.red_500)
                        else -> ContextCompat.getColor(context, R.color.red_500)
                    }
            ))

            tvJobTitle.text = title

            // fixme: export string resource
            val statusBuilder = StringBuffer("Status: ").append(jobStatusName(status))
            if (statusDetail !== null) {
                statusBuilder.append(". ").append(statusDetail)
            }
            tvJobStatus.text = statusBuilder.toString()

            val path = getPath(this)
            tvJobLocation.text = context.getString(R.string.job_output_location,
                    path ?: command.output)

            buttonLayout.visibility = if (status == JobStatus.COMPLETED) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun cancelJob(context: Context, job: Job, deleteFile: Boolean) {
        ConverterService.cancelJob(context, jobId = job.id)
        val outputUri = Uri.parse(job.command.output)
        if (deleteFile) {
            if (outputUri.scheme == ContentResolver.SCHEME_CONTENT) {
                catchAll { context.contentResolver.delete(outputUri, null, null) }
            } else if (outputUri.scheme == ContentResolver.SCHEME_FILE) {
                catchAll { File(outputUri.path).delete() }
            }

            // delete media store entry
            val realPath = catchAll { UriUtils.getPathFromUri(context, outputUri) }
            if (realPath !== null) {
                MediaScannerConnection.scanFile(context, arrayOf(realPath), null, null)
            }
        }
    }
}
