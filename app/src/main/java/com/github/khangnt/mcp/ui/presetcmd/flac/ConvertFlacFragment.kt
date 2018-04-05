package com.github.khangnt.mcp.ui.presetcmd.flac

import android.annotation.SuppressLint
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
import com.github.khangnt.mcp.util.onSeekBarChanged
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
        btnStartConversion.setOnClickListener { validateAndStartConversion() }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        updateQualityText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateQualityText() {
        tvCompressionLevel.text = "${sbCompressionLevel.progress}"
    }

    private fun getIoFragment(): SingleInputOutputFragment {
        val fragment = childFragmentManager.findFragmentById(R.id.fragmentInputOutput)
        return fragment as SingleInputOutputFragment
    }

    private fun validateAndStartConversion() {
        getIoFragment().validateAndGetInputOutputData { inputOutputData ->
            val cmdArgsBuilder = StringBuffer()
            cmdArgsBuilder.append("-hide_banner -map 0:a -map_metadata 0:g -codec:a flac ")
                    .append("-compression_level ${sbCompressionLevel.progress} ")

            val job = Job(
                    title = inputOutputData.title,
                    command = Command(
                            inputs = listOf(inputOutputData.inputUri),
                            output = inputOutputData.outputUri,
                            outputFormat = "flac",
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