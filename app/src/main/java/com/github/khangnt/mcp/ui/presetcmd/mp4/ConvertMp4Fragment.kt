package com.github.khangnt.mcp.ui.presetcmd.mp4

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.presetcmd.ConvertFragment
import com.github.khangnt.mcp.ui.presetcmd.common.SingleInputOutputFragment
import com.github.khangnt.mcp.util.onItemSelected
import com.github.khangnt.mcp.util.onSeekBarChanged
import com.github.khangnt.mcp.worker.ConverterService
import kotlinx.android.synthetic.main.fragment_convert_mp4.*
import timber.log.Timber


/**
 * GUI helps create convert mp4 command, likes:
 * ffmpeg -i input.avi -c:v libx264 -preset slow -crf 22 -c:a copy output.mkv
 */
class ConvertMp4Fragment : ConvertFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_mp4   , container, false)

    companion object {
        // https://trac.ffmpeg.org/wiki/Encode/H.264
        private val mp4Presets = arrayOf(
                "ultrafast", "superfast", "veryfast",
                "faster", "fast", "medium",
                "slow", "slower", "veryslow"
        )

        private const val CBR_MIN = 45  // 45 kbps
        private const val CBR_MAX = 320 // 320 kbps
        private const val CBR_RECOMMEND = 256
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIoFragment().autoFillExt = "mp4"
//        sbQuality.onSeekBarChanged { updateQualityText() }
//        spinnerEncoder.onItemSelected { position ->
//            when (position) {
//                0 -> {
//                    // libMp3lame
//                    if (sbQuality.max != 9) {
//                        sbQuality.progress = 9
//                        sbQuality.max = 9
//                    }
//                }
//                1 -> {
//                    if (sbQuality.max != CBR_MAX - CBR_MIN) {
//                        sbQuality.max = CBR_MAX - CBR_MIN
//                        sbQuality.progress = CBR_RECOMMEND - CBR_MIN
//                    }
//                }
//            }
//        }

        btnStartConversion.setOnClickListener { validateAndStartConversion() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateQualityText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
//        if (spinnerEncoder.selectedItemPosition == 0 && sbQuality.progress <= 9) {
//            tvQualityValue.text = "${libMp3LameQuality[9 - sbQuality.progress]} kbps"
//        } else {
//            tvQualityValue.text = "${sbQuality.progress + CBR_MIN} kbps"
//        }
    }

    private fun getIoFragment(): SingleInputOutputFragment {
        val fragment = childFragmentManager.findFragmentById(R.id.fragmentInputOutput)
        return fragment as SingleInputOutputFragment
    }

    private fun validateAndStartConversion() {
        getIoFragment().validateAndGetInputOutputData { inputOutputData ->
            val cmdArgsBuilder = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g -c:v libx264 -acodec aac -preset medium ")

//            cmdArgsBuilder.append("-preset ${mp4Presets[spinnerPresets.selectedItemPosition]} ")

            ConverterService.newJob(
                    context!!,
                    title = inputOutputData.title,
                    inputs = listOf(inputOutputData.inputUri),
                    args = cmdArgsBuilder.toString(),
                    outputUri = inputOutputData.outputUri,
                    outputFormat = "mp4"
            )

            (activity as? OnSubmittedListener)?.onSubmitted(this)
                    ?: Timber.w("Host activity does not implement OnSubmittedListener")
        }
    }

    override fun shouldConfirmDiscardChanges(): Boolean =
            getIoFragment().shouldConfirmDiscardChanges()

}