package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.aac

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.annotation.Muxer
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import com.github.khangnt.mcp.util.onSeekBarChanged
import com.github.khangnt.mcp.util.parseFileName
import com.github.khangnt.mcp.util.parseInputUri
import kotlinx.android.synthetic.main.fragment_convert_aac.*
import timber.log.Timber

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class AacCmdBuilderFragment : CommandBuilderFragment() {

    companion object {
        fun create(inputFiles: List<String>) = AacCmdBuilderFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(ARG_INPUT_FILES, ArrayList(inputFiles))
            }
        }

        private const val CBR_MIN = 45  // 45 kbps
        private const val CBR_MAX = 320 // 320 kbps
        private const val CBR_RECOMMEND = 256
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_aac, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sbQuality.max = CBR_MAX - CBR_MIN
        sbQuality.progress = CBR_RECOMMEND - CBR_MIN
        sbQuality.onSeekBarChanged { updateQualityText() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateQualityText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
        tvQualityValue.text = "${sbQuality.progress + CBR_MIN} kbps"
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        onSuccess(AacCmdConfig(inputFiles, CBR_MIN + sbQuality.progress))
    }
}

class AacCmdConfig(
        inputFiles: List<String>,
        private val quality: Int
) : CommandConfig(inputFiles) {

    override fun getNumberOfOutput(): Int = inputFiles.size // 1 input - 1 output

    override fun generateOutputFileNames(): List<String> {
        return inputFiles.map {
            val inputFileName = it.parseInputUri().lastPathSegment?.trim()
            if (inputFileName == null || inputFileName.isEmpty()) {
                return@map "Untitled.aac"
            }
            val (name, extension) = inputFileName.parseFileName()
            Timber.d("File '$name' ext '$extension'")
            return@map "$name.aac"
        }
    }

    override fun getOutputFileNameExt(): String {
        return "aac"
    }

    override fun makeJobs(finalOutputs: List<Output>): List<Job> {
        check(finalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g ")
                .append("-codec:a aac ")
                .append("-b:a ${quality}k ")
                .toString()
        return finalOutputs.mapIndexed { index, output ->
            Job(
                    title = output.title,
                    command = Command(
                            listOf(inputFiles[index]), output.outputUri, // single input output
                            Muxer.IPOD, cmdArgs, emptyMap()
                    )
            )
        }
    }

}