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
import com.github.khangnt.mcp.util.gone
import com.github.khangnt.mcp.util.onItemSelected
import com.github.khangnt.mcp.util.onSeekBarChanged
import com.github.khangnt.mcp.util.visible
import kotlinx.android.synthetic.main.fragment_convert_mp3.*
import java.lang.IllegalStateException

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class Mp3CmdBuilderFragment : CommandBuilderFragment() {

    companion object {
        // https://trac.ffmpeg.org/wiki/Encode/MP3
        private val libMp3LameQuality = arrayOf(
                "220-260", "190-250", "170-210", "150-195", "140-185",
                "120-150", "100-130", "80-120", "70-105", "45-85"
        )

        private const val CBR_MIN = 45  // 45 kbps
        private const val CBR_MAX = 320 // 320 kbps
        private const val CBR_RECOMMEND = 256

        private const val VBR_MAX = 9
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_mp3, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sbQualityLame.max = VBR_MAX
        sbQualityLame.progress = VBR_MAX
        sbQualityShine.max = CBR_MAX - CBR_MIN
        sbQualityShine.progress = CBR_RECOMMEND - CBR_MIN

        sbQualityLame.onSeekBarChanged { updateQualityText() }
        sbQualityShine.onSeekBarChanged { updateQualityText() }
        spinnerEncoder.onItemSelected { position ->
            when (position) {
                0 -> { // libMp3lame
                    sbQualityLame.visible()
                    sbQualityShine.gone()
                }
                1 -> { // libShine
                    sbQualityShine.visible()
                    sbQualityLame.gone()
                }
            }
            updateQualityText()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
        if (spinnerEncoder.selectedItemPosition == 0 && sbQualityLame.progress <= 9) {
            tvQualityValue.text = "${libMp3LameQuality[VBR_MAX - sbQualityLame.progress]} kbps"
        } else {
            tvQualityValue.text = "${sbQualityShine.progress + CBR_MIN} kbps"
        }
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        if (spinnerEncoder.selectedItemPosition == 0) {
            val encoder = Encoders.LIBMP3LAME
            val quality = VBR_MAX - sbQualityLame.progress
            onSuccess(Mp3CmdConfig(inputFileUris, encoder, QualityType.VBR, quality, cbTrimSilence.isChecked))
        } else {
            val encoder = Encoders.LIBSHINE
            val quality = CBR_MIN + sbQualityShine.progress
            onSuccess(Mp3CmdConfig(inputFileUris, encoder, QualityType.CBR, quality, cbTrimSilence.isChecked))
        }
    }
}

class Mp3CmdConfig(
        inputFiles: List<String>,
        @Mp3Encoder private val encoder: String,
        @QualityType private val qualityType: Int,
        private val quality: Int,
        private val isTrimSilence: Boolean
) : CommandConfig(inputFiles) {

    init {
        check(encoder == Encoders.LIBMP3LAME || encoder == Encoders.LIBSHINE) {
            "Invalid mp3 encoder: $encoder"
        }
    }

    override fun getNumberOfOutput(): Int = inputFileUris.size // 1 input - 1 output

    override fun generateOutputFiles(): List<AutoGenOutput> {
        return List(inputFileUris.size, { i -> AutoGenOutput(getFileNameFromInputs(i), "mp3") })
    }

    override fun makeJobs(finalFinalOutputs: List<FinalOutput>): List<Job> {
        check(finalFinalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g ")
                .append("-codec:a $encoder ")
                .append(when (qualityType) {
                    QualityType.CBR -> "-b:a ${quality}k "
                    QualityType.VBR -> "-q:a $quality "
                    else -> throw IllegalStateException("Unknown quality type: $qualityType")
                })
                .append(when (isTrimSilence) {
                    true -> "-af silenceremove=1:0:-50dB:1:1:-50dB "
                    false -> ""
                })
                .toString()
        return finalFinalOutputs.mapIndexed { index, output ->
            Job(
                    title = output.title,
                    command = Command(
                            listOf(inputFileUris[index]), output.outputUri, // single input output
                            Muxer.MP3, cmdArgs, emptyMap()
                    )
            )
        }
    }

}