package com.github.khangnt.mcp.ui.presetcmd.mp4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.ui.presetcmd.ConvertFragment
import com.github.khangnt.mcp.ui.presetcmd.common.SingleInputOutputFragment
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

        btnStartConversion.setOnClickListener { validateAndStartConversion() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    private fun getIoFragment(): SingleInputOutputFragment {
        val fragment = childFragmentManager.findFragmentById(R.id.fragmentInputOutput)
        return fragment as SingleInputOutputFragment
    }

    private fun validateAndStartConversion() {
        getIoFragment().validateAndGetInputOutputData { inputOutputData ->
            val cmdArgsBuilder = StringBuffer("-hide_banner -map_metadata 0:g -map 0:v -map '0:a?' -map '0:s?' -c:v mpeg4 -c:a aac -c:s srt ")

            cmdArgsBuilder.append("-q:v ${mp4VideoQuality[spinnerVideoQuality.selectedItemPosition]} ")

            val job = Job(
                    title = inputOutputData.title,
                    command = Command(
                            inputs = listOf(inputOutputData.inputUri),
                            output = inputOutputData.outputUri,
                            outputFormat = "mp4",
                            args = cmdArgsBuilder.toString(),
                            environmentVars = emptyMap()
                    )
            )
            SingletonInstances.getJobWorkerMangager().addJob(job)

            (activity as? OnSubmittedListener)?.onSubmitted(this)
                    ?: Timber.w("Host activity does not implement OnSubmittedListener")
        }
    }

    override fun shouldConfirmDiscardChanges(): Boolean =
            getIoFragment().shouldConfirmDiscardChanges()

}