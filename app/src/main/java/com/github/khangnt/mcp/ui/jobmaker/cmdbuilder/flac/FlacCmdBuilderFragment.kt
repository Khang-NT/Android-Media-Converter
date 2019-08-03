package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.flac

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.annotation.Muxer
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import com.github.khangnt.mcp.util.onSeekBarChanged
import kotlinx.android.synthetic.main.fragment_convert_flac.*
import org.json.JSONObject

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class FlacCmdBuilderFragment : CommandBuilderFragment() {

    private val sharedPrefs = SingletonInstances.getSharedPrefs()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_flac, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sbCompressionLevel.onSeekBarChanged { updateQualityText() }

        // restore command configs
        if (sharedPrefs.rememberCommandConfig && savedInstanceState == null) {
            val lastConfig = JSONObject(sharedPrefs.lastFlacConfigs)
            val compressionLevel = lastConfig.optInt("compressionLevel",
                    sbCompressionLevel.progress)
            val isTrimSilence = lastConfig.optBoolean("isTrimSilence",
                    cbTrimSilence.isChecked)

            sbCompressionLevel.progress = compressionLevel
            cbTrimSilence.isChecked = isTrimSilence
        }
    }

    override fun onDestroyView() {
        // save command configs
        if (sharedPrefs.rememberCommandConfig) {
            val lastConfig = JSONObject()
            lastConfig.put("compressionLevel", sbCompressionLevel.progress)
            lastConfig.put("isTrimSilence", cbTrimSilence.isChecked)
            sharedPrefs.lastFlacConfigs = lastConfig.toString()
        }
        super.onDestroyView()
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
        onSuccess(FlacCmdConfig(inputFileUris, sbCompressionLevel.progress, cbTrimSilence.isChecked))
    }
}

class FlacCmdConfig(
        inputFiles: List<String>,
        private val compressionLevel: Int,
        private val isTrimSilence: Boolean
) : CommandConfig(inputFiles) {

    override fun getNumberOfOutput(): Int = inputFileUris.size // 1 input - 1 output

    override fun generateOutputFiles(): List<AutoGenOutput> {
        return List(inputFileUris.size, { i -> AutoGenOutput(getFileNameFromInputs(i), "flac") })
    }

    override fun makeJobs(finalFinalOutputs: List<FinalOutput>): List<Job> {
        check(finalFinalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g ")
                .append("-codec:a flac ")
                .append("-compression_level $compressionLevel ")
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
                            Muxer.FLAC, cmdArgs, emptyMap()
                    )
            )
        }
    }

}