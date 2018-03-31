package com.github.khangnt.mcp.ui.presetcmd.flac

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
import kotlinx.android.synthetic.main.fragment_convert_flac.*
import timber.log.Timber

/**
 * Created by Khang NT on 2/23/18.
 * Email: khang.neon.1997@gmail.com
 */

class ConvertFlacFragment : ConvertFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_flac, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIoFragment().autoFillExt = "flac"
        sbCompressionLevel.onSeekBarChanged { updateQualityText() }

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
        tvCompressionLevel.text = "${sbCompressionLevel.progress}"
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
            cmdArgsBuilder.append("-hide_banner -map 0:a -map_metadata 0:g -codec:a flac ")
                    .append("-compression_level ${sbCompressionLevel.progress} ")


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
                            outputFormat = "flac"
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