package com.github.khangnt.mcp.ui.presetcmd.mp4

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
import com.github.khangnt.mcp.worker.ConverterService
import kotlinx.android.synthetic.main.fragment_convert_mp4.*
import timber.log.Timber


/**
 * GUI helps create convert mp4 command, likes:
 * ffmpeg -i input.avi -c:v mpeg4 -qscale:v 3 -c:a aac -qscale:a 4 output.mp4
 */
class ConvertMp4Fragment : ConvertFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_mp4   , container, false)

    companion object {
        // https://trac.ffmpeg.org/wiki/Encode/MPEG-4
        private val mp4VideoQuality = arrayOf(
                "2", "6", "12", "18", "26"
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIoFragment().autoFillExt = "mp4"
        spinnerVideoQuality.setSelection(1)

        btnAdvancedToggle.setOnClickListener { toggleAdvanced() }
        btnStartConversion.setOnClickListener { validateAndStartConversion() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
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
            val cmdArgsBuilder = StringBuffer("-hide_banner -map_metadata 0:g -map 0:v -map '0:a?' -map '0:s?' -c:v mpeg4 -c:a aac -c:s srt ")

            cmdArgsBuilder.append("-q:v ${mp4VideoQuality[spinnerVideoQuality.selectedItemPosition]} ")


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
                            outputFormat = "mp4"
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