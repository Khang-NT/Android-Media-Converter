package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.mp3

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.annotation.Encoders
import com.github.khangnt.mcp.annotation.Mp3Encoder
import com.github.khangnt.mcp.annotation.Muxer
import com.github.khangnt.mcp.annotation.QualityType
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import com.github.khangnt.mcp.util.onItemSelected
import com.github.khangnt.mcp.util.onSeekBarChanged
import com.github.khangnt.mcp.util.parseFileName
import com.github.khangnt.mcp.util.parseInputUri
import kotlinx.android.synthetic.main.fragment_convert_mp3.*
import timber.log.Timber
import java.lang.IllegalStateException

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class Mp3CmdBuilderFragment : CommandBuilderFragment() {

    companion object {
        fun create(inputFiles: List<String>) = Mp3CmdBuilderFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(ARG_INPUT_FILES, ArrayList(inputFiles))
            }
        }

        // https://trac.ffmpeg.org/wiki/Encode/MP3
        private val libMp3LameQuality = arrayOf(
                "220-260", "190-250", "170-210", "150-195", "140-185",
                "120-150", "100-130", "80-120", "70-105", "45-85"
        )

        private const val CBR_MIN = 45  // 45 kbps
        private const val CBR_MAX = 320 // 320 kbps
        private const val CBR_RECOMMEND = 256
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_mp3, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sbQuality.onSeekBarChanged { updateQualityText() }
        spinnerEncoder.onItemSelected { position ->
            when (position) {
                0 -> {
                    // libMp3lame
                    if (sbQuality.max != 9) {
                        sbQuality.progress = 9
                        sbQuality.max = 9
                    }
                }
                1 -> {
                    if (sbQuality.max != CBR_MAX - CBR_MIN) {
                        sbQuality.max = CBR_MAX - CBR_MIN
                        sbQuality.progress = CBR_RECOMMEND - CBR_MIN
                    }
                }
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateQualityText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
        if (spinnerEncoder.selectedItemPosition == 0 && sbQuality.progress <= 9) {
            tvQualityValue.text = "${libMp3LameQuality[9 - sbQuality.progress]} kbps"
        } else {
            tvQualityValue.text = "${sbQuality.progress + CBR_MIN} kbps"
        }
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        if (spinnerEncoder.selectedItemPosition == 0) {
            val encoder = Encoders.LIBMP3LAME
            val quality = 9 - sbQuality.progress
            onSuccess(Mp3CmdConfig(inputFiles, encoder, QualityType.VBR, quality))
        } else {
            val encoder = Encoders.LIBSHINE
            val quality = CBR_MIN + sbQuality.progress
            onSuccess(Mp3CmdConfig(inputFiles, encoder, QualityType.CBR, quality))
        }
    }
}

class Mp3CmdConfig(
        inputFiles: List<String>,
        @Mp3Encoder private val encoder: String,
        @QualityType private val qualityType: Int,
        private val quality: Int
) : CommandConfig(inputFiles) {

    init {
        check(encoder == Encoders.LIBMP3LAME || encoder == Encoders.LIBSHINE) {
            "Invalid mp3 encoder: $encoder"
        }
    }

    override fun getNumberOfOutput(): Int = inputFiles.size // 1 input - 1 output

    override fun generateOutputFileNames(): List<String> {
        return inputFiles.map {
            val inputFileName = it.parseInputUri().lastPathSegment?.trim()
            if (inputFileName == null || inputFileName.isEmpty()) {
                return@map "Untitled.mp3"
            }
            val (name, extension) = inputFileName.parseFileName()
            Timber.d("File '$name' ext '$extension'")
            return@map "$name.mp3"
        }
    }

    override fun getOutputFileNameExt(): String {
        return "mp3"
    }

    override fun makeJobs(finalOutputs: List<Output>): List<Job> {
        check(finalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g ")
                .append("-codec:a $encoder ")
                .append(when (qualityType) {
                    QualityType.CBR -> "-b:a ${quality}k "
                    QualityType.VBR -> "-q:a $quality "
                    else -> throw IllegalStateException("Unknown quality type: $qualityType")
                })
                .toString()
        return finalOutputs.mapIndexed { index, output ->
            Job(
                    title = output.title,
                    command = Command(
                            listOf(inputFiles[index]), output.outputUri, // single input output
                            Muxer.MP3, cmdArgs, emptyMap()
                    )
            )
        }
    }

}