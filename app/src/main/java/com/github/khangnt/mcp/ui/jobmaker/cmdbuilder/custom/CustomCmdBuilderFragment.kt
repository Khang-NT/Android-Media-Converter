package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.custom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandConfig
import com.github.khangnt.mcp.util.onTextChanged
import kotlinx.android.synthetic.main.fragment_convert_custom.*
import org.json.JSONObject

/**
 * Created by Simon Pham on 02/10/20.
 * Email: simon@simonit.dev
 */

class CustomCmdBuilderFragment : CommandBuilderFragment() {

    private val sharedPrefs = SingletonInstances.getSharedPrefs()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_custom, container, false)

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // restore command configs
        if (sharedPrefs.rememberCommandConfig && savedInstanceState == null) {
            val lastConfig = JSONObject(sharedPrefs.lastCustomConfigs)
            val lastCustomCommand = lastConfig.optString("lastCustomCommand", "")
            val fileExtension = lastConfig.optString("fileExtension", "")
            val outputMuxer = lastConfig.optString("outputMuxer", "")
            val hasRecommendedParams = lastConfig.optBoolean("hasRecommendedParams",
                    cbAddRecommendParams.isChecked)
            val hasRecommendedForAudio = lastConfig.optBoolean("hasRecommendedParamsForAudio",
                    cbMapAudio.isChecked)
            var hasRecommendedForVideo = lastConfig.optBoolean("hasRecommendedForVideo",
                    cbMapAudioVideoSrt.isChecked)
            val isTrimSilence = lastConfig.optBoolean("isTrimSilence",
                    cbTrimSilence.isChecked)

            if (hasRecommendedForAudio && hasRecommendedForVideo) {
                hasRecommendedForVideo = false
            }

            cbMapAudio.setOnCheckedChangeListener { _, isChecked ->
                cbMapAudioVideoSrt.isEnabled = !isChecked
            }

            cbMapAudioVideoSrt.setOnCheckedChangeListener { _, isChecked ->
                cbMapAudio.isEnabled = !isChecked
            }

            edFileExtension.onTextChanged { text ->
                val muxer = if (edOutputMuxer.text.isEmpty()) {
                    if (text.isEmpty()) {
                        edFileExtension.hint
                    } else {
                        text
                    }
                } else {
                    edOutputMuxer.text
                }

                edOutputMuxer.hint = "Default: $muxer"
                edOutputMuxerHint.text = "-y -f $muxer"
            }

            edOutputMuxer.onTextChanged { text ->
                val extension = if (edFileExtension.text.isEmpty()) {
                    edFileExtension.hint
                } else {
                    edFileExtension.text
                }

                val muxer = if (text.isEmpty()) {
                    extension
                } else {
                    text
                }
                edOutputMuxer.hint = "Default: $muxer"
                edOutputMuxerHint.text = "-y -f $muxer"
            }

            edCustomCommand.setText(lastCustomCommand)
            edFileExtension.setText(fileExtension)
            edOutputMuxer.setText(outputMuxer)
            cbAddRecommendParams.isChecked = hasRecommendedParams
            cbMapAudio.isChecked = hasRecommendedForAudio
            cbMapAudioVideoSrt.isChecked = hasRecommendedForVideo
            cbTrimSilence.isChecked = isTrimSilence
        }
    }

    override fun onDestroyView() {
        // save command configs
        if (sharedPrefs.rememberCommandConfig) {
            val lastConfig = JSONObject()
            lastConfig.put("lastCustomCommand", edCustomCommand.text.toString())
            lastConfig.put("fileExtension", edFileExtension.text.toString())
            lastConfig.put("outputMuxer", edOutputMuxer.text.toString())
            lastConfig.put("hasRecommendedParams", cbAddRecommendParams.isChecked)
            lastConfig.put("hasRecommendedParamsForAudio", cbMapAudio.isChecked)
            lastConfig.put("hasRecommendedForVideo", cbMapAudioVideoSrt.isChecked)
            lastConfig.put("isTrimSilence", cbTrimSilence.isChecked)
            sharedPrefs.lastCustomConfigs = lastConfig.toString()
        }
        super.onDestroyView()
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        var extension = edFileExtension.text.trim().toString()
        var muxer = edOutputMuxer.text.trim().toString()
        if (extension.isEmpty()) {
            extension = edFileExtension.hint.toString()
        }
        if (muxer.isEmpty()) {
            muxer = extension
        }

        val hasRecommendedParams = cbAddRecommendParams.isChecked
        val hasRecommendedParamsForAudio = cbMapAudio.isChecked
        val hasRecommendedForVideo = cbMapAudioVideoSrt.isChecked
        val isTrimSilence = cbTrimSilence.isChecked

        val customCommand = StringBuffer(edCustomCommand.text.trim().toString() + " ")
                .append(when (hasRecommendedParams) {
                    true -> "-hide_banner -map_metadata 0:g "
                    false -> ""
                })
                .append(when (hasRecommendedParamsForAudio) {
                    true -> "-map 0:a "
                    false -> ""
                })
                .append(when (hasRecommendedForVideo) {
                    true -> "-map 0:v -map '0:a?' -map '0:s?' -c:v mpeg4 -c:a aac -c:s srt "
                    false -> ""
                })
                .append(when (isTrimSilence) {
                    true -> "-af silenceremove=1:0:-50dB:1:1:-50dB "
                    false -> ""
                })
                .toString()
        onSuccess(CustomCmdConfig(inputFileUris, customCommand, extension, muxer))
    }
}

class CustomCmdConfig(
        inputFiles: List<String>,
        private val customCommand: String,
        private val fileExtension: String,
        private val outputMuxer: String
) : CommandConfig(inputFiles) {

    override fun getNumberOfOutput(): Int = inputFileUris.size // 1 input - 1 output

    override fun generateOutputFiles(): List<AutoGenOutput> {
        return List(inputFileUris.size) { i -> AutoGenOutput(getFileNameFromInputs(i), fileExtension) }
    }

    override fun makeJobs(finalFinalOutputs: List<FinalOutput>): List<Job> {
        check(finalFinalOutputs.size == getNumberOfOutput())
        return finalFinalOutputs.mapIndexed { index, output ->
            Job(
                    title = output.title,
                    command = Command(
                            listOf(inputFileUris[index]), output.outputUri, // single input output
                            outputMuxer, customCommand, emptyMap()
                    )
            )
        }
    }

}