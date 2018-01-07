package com.github.khangnt.mcp.ui.job_manager

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatTextView
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
                JobStatus.FAILED -> "FAILED"
                JobStatus.COMPLETED -> "SUCCESS"
                else -> "$jobStatus"
            }
        }
    }

    private val tvFileFmt by lazy { itemView.findViewById<AppCompatTextView>(R.id.tvOutputFormat) }
    private val tvJobTitle by lazy { itemView.findViewById<TextView>(R.id.tvJobTitle) }
    private val tvJobStatus by lazy { itemView.findViewById<TextView>(R.id.tvJobStatus) }
    private val tvJobLocation by lazy { itemView.findViewById<TextView>(R.id.tvJobLocation) }
    private val ivDeleteJob by lazy { itemView.findViewById<ImageView>(R.id.ivCancelJob) }

    @SuppressLint("SetTextI18n")
    override fun bind(model: JobModel, pos: Int) {
        model.job.apply {
            tvFileFmt.text = command.outputFormat
                    .substring(0, minOf(3, command.outputFormat.length)).toUpperCase()
            val context = tvFileFmt.context
            ViewCompat.setBackgroundTintList(tvFileFmt, ColorStateList.valueOf(
                    when (status) {
                        RUNNING -> ContextCompat.getColor(context, R.color.teal_500)
                        PENDING -> ContextCompat.getColor(context, R.color.blue_grey_500)
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

            var path = cacheOutputPath.get(id.toInt())
            if (path === null) {
                catchAll { path = UriUtils.getPathFromUri(context, Uri.parse(command.output)) }
                if (path !== null) {
                    cacheOutputPath.put(id.toInt(), path)
                }
            }
            tvJobLocation.text = context.getString(R.string.job_output_location,
                    path ?: command.output)

            ivDeleteJob.setOnClickListener {
                ConverterService.cancelJob(context, jobId = id)
            }
        }
    }
}
