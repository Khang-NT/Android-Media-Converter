package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.mp3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.annotation.Encoders
import com.github.khangnt.mcp.annotation.Mp3Encoder
import com.github.khangnt.mcp.annotation.Muxer
import com.github.khangnt.mcp.annotation.QualityType
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import kotlinx.android.synthetic.main.fragment_convert_mp3.*

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class Mp3CmdBuilderFragment : CommandBuilderFragment() {

    companion object {
        // https://trac.ffmpeg.org/wiki/Encode/MP3
        private val cbrBitrate = arrayOf(
                320, 256, 224, 192, 128,
                96, 80, 64, 48, 32
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_mp3, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mp3VbrAdapter = ArrayAdapter.createFromResource(context!!,
                R.array.mp3VbrBitrate, android.R.layout.simple_spinner_item)
        val cbrAdapter = ArrayAdapter.createFromResource(context!!,
                R.array.cbrBitrate, android.R.layout.simple_spinner_item)
        mp3VbrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cbrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        rgBitrateType.setOnCheckedChangeListener { _, checkedId ->
            spinnerBitrate.adapter = if (checkedId == R.id.radioVbr) mp3VbrAdapter else cbrAdapter
        }
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        val isVbrChecked = rgBitrateType.checkedRadioButtonId == R.id.radioVbr
        val qualityType = if (isVbrChecked) QualityType.VBR else QualityType.CBR
        val quality = if (isVbrChecked)
            spinnerBitrate.selectedItemPosition else cbrBitrate[spinnerBitrate.selectedItemPosition]
        val encoder = if (spinnerEncoder.selectedItemPosition == 0)
            Encoders.LIBMP3LAME else Encoders.LIBSHINE

        onSuccess(Mp3CmdConfig(inputFileUris, encoder, qualityType, quality, cbTrimSilence.isChecked))
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
        return List(inputFileUris.size) { i -> AutoGenOutput(getFileNameFromInputs(i), "mp3") }
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