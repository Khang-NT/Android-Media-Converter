package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.opus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.annotation.Encoders.LIBOPUS
import com.github.khangnt.mcp.annotation.Muxer
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import com.github.khangnt.mcp.util.onSeekBarChanged
import kotlinx.android.synthetic.main.fragment_convert_opus.*

/**
 * Created by Simon Pham on 5/28/18.
 * Email: simonpham.dn@gmail.com
 */

class OpusCmdBuilderFragment : CommandBuilderFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_opus, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sbAudioQuality.onSeekBarChanged { updateQualityText() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateQualityText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
        tvAudioQuality.text = "${sbAudioQuality.progress}"
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        onSuccess(OpusCmdConfig(inputFileUris, 10 - sbAudioQuality.progress, cbTrimSilence.isChecked))
    }
}

class OpusCmdConfig(
        inputFiles: List<String>,
        private val quality: Int,
        private val isTrimSilence: Boolean
) : CommandConfig(inputFiles) {

    override fun getNumberOfOutput(): Int = inputFileUris.size // 1 input - 1 output

    override fun generateOutputFiles(): List<AutoGenOutput> {
        return List(inputFileUris.size, { i -> AutoGenOutput(getFileNameFromInputs(i), "opus") })
    }

    override fun makeJobs(finalFinalOutputs: List<FinalOutput>): List<Job> {
        check(finalFinalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g ")
                .append("-codec:a $LIBOPUS ")
                .append("-compression_level $quality ")
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
                            Muxer.OPUS, cmdArgs, emptyMap()
                    )
            )
        }
    }

}