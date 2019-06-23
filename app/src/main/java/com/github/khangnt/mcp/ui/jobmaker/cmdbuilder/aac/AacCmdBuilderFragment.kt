package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.aac

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
import kotlinx.android.synthetic.main.fragment_convert_aac.*

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class AacCmdBuilderFragment : CommandBuilderFragment() {

    companion object {
        private val cbrBitrate = arrayOf(
                320, 256, 224, 192, 128,
                96, 80, 64, 48, 32
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_aac, container, false)

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        val quality = cbrBitrate[spinnerBitrate.selectedItemPosition]
        onSuccess(AacCmdConfig(inputFileUris, quality, cbTrimSilence.isChecked))
    }
}

class AacCmdConfig(
        inputFiles: List<String>,
        private val quality: Int,
        private val isTrimSilence: Boolean
) : CommandConfig(inputFiles) {

    override fun getNumberOfOutput(): Int = inputFileUris.size // 1 input - 1 output

    override fun generateOutputFiles(): List<AutoGenOutput> {
        return List(inputFileUris.size) { i -> AutoGenOutput(getFileNameFromInputs(i), "aac") }
    }

    override fun makeJobs(finalFinalOutputs: List<FinalOutput>): List<Job> {
        check(finalFinalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g ")
                .append("-codec:a aac ")
                .append("-b:a ${quality}k ")
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
                            Muxer.IPOD, cmdArgs, emptyMap()
                    )
            )
        }
    }

}