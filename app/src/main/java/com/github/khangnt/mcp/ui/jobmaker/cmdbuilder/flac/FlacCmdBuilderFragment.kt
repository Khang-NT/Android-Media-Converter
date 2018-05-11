package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.flac

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
import kotlinx.android.synthetic.main.fragment_convert_flac.*
import timber.log.Timber

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class FlacCmdBuilderFragment : CommandBuilderFragment() {

    companion object {
        fun create(inputFiles: List<String>) = FlacCmdBuilderFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(ARG_INPUT_FILES, ArrayList(inputFiles))
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_flac, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sbCompressionLevel.onSeekBarChanged { updateQualityText() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateQualityText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
        tvCompressionLevel.text = "${sbCompressionLevel.progress}"
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        onSuccess(FlacCmdConfig(inputFiles, sbCompressionLevel.progress))
    }
}

class FlacCmdConfig(
        inputFiles: List<String>,
        private val compressionLevel: Int
) : CommandConfig(inputFiles) {

    override fun getNumberOfOutput(): Int = inputFiles.size // 1 input - 1 output

    override fun generateOutputFileNames(): List<String> {
        return inputFiles.map {
            val inputFileName = it.parseInputUri().lastPathSegment?.trim()
            if (inputFileName == null || inputFileName.isEmpty()) {
                return@map "Untitled.flac"
            }
            val (name, extension) = inputFileName.parseFileName()
            Timber.d("File '$name' ext '$extension'")
            return@map "$name.flac"
        }
    }

    override fun getOutputFileNameExt(): String {
        return "flac"
    }

    override fun makeJobs(finalOutputs: List<Output>): List<Job> {
        check(finalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g ")
                .append("-codec:a flac ")
                .append("-compression_level $compressionLevel ")
                .toString()
        return finalOutputs.mapIndexed { index, output ->
            Job(
                    title = output.title,
                    command = Command(
                            listOf(inputFiles[index]), output.outputUri, // single input output
                            Muxer.FLAC, cmdArgs, emptyMap()
                    )
            )
        }
    }

}