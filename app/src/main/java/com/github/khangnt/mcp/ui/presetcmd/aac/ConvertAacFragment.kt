package com.github.khangnt.mcp.ui.presetcmd.aac

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
import com.github.khangnt.mcp.util.onSeekBarChanged
import com.github.khangnt.mcp.worker.ConverterService
import kotlinx.android.synthetic.main.fragment_convert_aac.*
import timber.log.Timber

/**
 * Created by Khang NT on 2/23/18.
 * Email: khang.neon.1997@gmail.com
 */

class ConvertAacFragment : ConvertFragment() {

    companion object {
        private const val CBR_MIN = 45  // 45 kbps
        private const val CBR_MAX = 320 // 320 kbps
        private const val CBR_RECOMMEND = 256
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_aac, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIoFragment().autoFillExt = "m4a"
        sbQuality.max = CBR_MAX - CBR_MIN
        sbQuality.progress = CBR_RECOMMEND - CBR_MIN
        sbQuality.onSeekBarChanged { updateQualityText() }

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
        tvQualityValue.text = "${sbQuality.progress + CBR_MIN} kbps"
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
            val cmdArgsBuilder = StringBuffer()
            cmdArgsBuilder.append("-hide_banner -map 0:a -map_metadata 0:g -codec:a aac ")
                    .append("-b:a ${CBR_MIN + sbQuality.progress}k ")

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
                            outputFormat = "ipod"
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