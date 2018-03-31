package com.github.khangnt.mcp.ui.presetcmd.mp3

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.transition.Fade
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.presetcmd.ConvertFragment
import com.github.khangnt.mcp.ui.presetcmd.common.SingleInputOutputFragment
import com.github.khangnt.mcp.ui.presetcmd.common.TrimmerFragment
import com.github.khangnt.mcp.util.onItemSelected
import com.github.khangnt.mcp.util.onSeekBarChanged
import com.github.khangnt.mcp.worker.ConverterService
import kotlinx.android.synthetic.main.fragment_convert_mp3.*
import timber.log.Timber


/**
 * GUI helps create convert mp3 command, likes:
 * ffmpeg -i input -codec:a libmp3lame -q:a 0 -f mp3 output.mp3
 * Or with libshine encoder:
 * ffmpeg -i input -codec:a libshine -b:a 256k mp3 output.mp3
 */
class ConvertMp3Fragment : ConvertFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_mp3, container, false)

    companion object {
        // https://trac.ffmpeg.org/wiki/Encode/MP3
        private val libMp3LameQuality = arrayOf(
                "220-260", "190-250", "170-210", "150-195", "140-185",
                "120-150", "100-130", "80-120", "70-105", "45-85"
        )

        private const val CBR_MIN = 45  // 45 kbps
        private const val CBR_MAX = 320 // 320 kbps
        private const val CBR_RECOMMEND = 256
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIoFragment().autoFillExt = "mp3"
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

        btnAdvancedToggle.setOnClickListener { toggleAdvanced() }

        btnStartConversion.setOnClickListener { validateAndStartConversion() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateQualityText()
    }

    private fun toggleAdvanced() {
        val transition = TransitionSet()
        transition.addTransition(Fade())
        transition.addTransition(Slide(Gravity.TOP))
        TransitionManager.beginDelayedTransition(advancedLayout, transition)

        if (advancedLayout.visibility == View.GONE) {
            advancedLayout.visibility = View.VISIBLE
            btnAdvancedToggle.text = getString(R.string.advanced_arrow_up)
        } else {
            advancedLayout.visibility = View.GONE
            btnAdvancedToggle.text = getString(R.string.advanced_arrow_down)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
        if (spinnerEncoder.selectedItemPosition == 0 && sbQuality.progress <= 9) {
            tvQualityValue.text = "${libMp3LameQuality[9 - sbQuality.progress]} kbps"
        } else {
            tvQualityValue.text = "${sbQuality.progress + CBR_MIN} kbps"
        }
    }

    private fun getIoFragment(): SingleInputOutputFragment {
        val fragment = childFragmentManager.findFragmentById(R.id.fragmentInputOutput)
        return fragment as SingleInputOutputFragment
    }

    private fun getTrimFragment(): TrimmerFragment {
        val fragment = childFragmentManager.findFragmentById(R.id.fragmentTrimmer)
        return fragment as TrimmerFragment
    }

    private fun validateAndStartConversion() {
        getIoFragment().validateAndGetInputOutputData { inputOutputData ->
            val cmdArgsBuilder = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g -codec:a ")
            if (spinnerEncoder.selectedItemPosition == 0) {
                cmdArgsBuilder.append("libmp3lame -q:a ${9 - sbQuality.progress} ")
            } else {
                cmdArgsBuilder.append("libshine -b:a ${CBR_MIN + sbQuality.progress}k ")
            }

            getTrimFragment().validateAndGetBeginEndPostition { beginEndPosition ->
                if (beginEndPosition.isTrimmed) {
                    cmdArgsBuilder.append("-ss ${beginEndPosition.beginPos} -t ${beginEndPosition.endPos} ")
                }

                if (!beginEndPosition.isError) {
                    ConverterService.newJob(
                            context!!,
                            title = inputOutputData.title,
                            inputs = listOf(inputOutputData.inputUri),
                            args = cmdArgsBuilder.toString(),
                            outputUri = inputOutputData.outputUri,
                            outputFormat = "mp3"
                    )

                    (activity as? OnSubmittedListener)?.onSubmitted(this)
                            ?: Timber.w("Host activity does not implement OnSubmittedListener")
                }
            }
        }
    }

    override fun shouldConfirmDiscardChanges(): Boolean =
            getIoFragment().shouldConfirmDiscardChanges()

}