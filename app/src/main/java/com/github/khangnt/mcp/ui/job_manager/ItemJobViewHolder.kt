package com.github.khangnt.mcp.ui.job_manager

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatTextView
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
import com.github.khangnt.mcp.worker.*
import org.json.JSONObject
import java.io.File

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

data class JobModel(val job: Job) : AdapterModel, HasIdModel {
    override val modelId: Long = job.id
}

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

            // fixme: make human readable location
            tvJobLocation.text = command.output

            ivDeleteJob.setOnClickListener {
                val uri = Uri.parse("https://r5---sn-42u-nbos6.googlevideo.com/videoplayback?initcwndbps=1018750&ipbits=0&clen=6170529&signature=E14FF731692ED88EEE8C9CA18CE25BA0B743E41B.289C39CFD4BB7F18F6E5E59B7C050DAEAA604AEF&mime=audio%2Fwebm&expire=1515229631&keepalive=yes&source=youtube&dur=335.681&pl=21&itag=251&mv=m&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Crequiressl%2Csource%2Cexpire&requiressl=yes&ei=Xz1QWt2wAs-j4AKEpaSQDQ&lmt=1493003596396005&id=o-ABxAP2zkm3duWQuX2njEz0sQ3vtxqpE7EqoCtDc_IL6R&mm=31&mn=sn-42u-nbos6&ms=au&mt=1515207911&gir=yes&key=yt6&ip=1.52.226.97&ratebypass=yes")
                val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)

                val intent = Intent(ivDeleteJob.context, ConverterService::class.java).apply {
                    action = ACTION_ADD_JOB
                    putExtra(EXTRA_JOB_TITLE, "test.mp3")
                    putStringArrayListExtra(EXTRA_JOB_CMD_INPUT_URIS, ArrayList(mutableListOf(uri.toString())))
                    putExtra(EXTRA_JOB_CMD_OUTPUT_URI, Uri.fromFile(File(musicDir, "Test.mp3")).toString())
                    putExtra(EXTRA_JOB_CMD_OUTPUT_FMT, "mp3")
                    putExtra(EXTRA_JOB_CMD_ARGS, "-codec:a libshine -b:a \$BITRATE")
                    putExtra(EXTRA_JOB_CMD_ENV_VARS_JSON, JSONObject(mapOf("BITRATE" to "320k")).toString())
                }
                ivDeleteJob.context.startService(intent)
            }
        }
    }
}
