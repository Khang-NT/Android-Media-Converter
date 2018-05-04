package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.annotation.Muxer
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.util.parseFileName
import com.github.khangnt.mcp.util.parseInputUri
import kotlinx.android.synthetic.main.fragment_convert_mp4.*
import timber.log.Timber

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class Mp4CmdBuilderFragment : CommandBuilderFragment() {

    companion object {
        fun create(inputFiles: List<String>) = Mp4CmdBuilderFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(ARG_INPUT_FILES, ArrayList(inputFiles))
            }
        }

        // https://trac.ffmpeg.org/wiki/Encode/MPEG-4
        private val mp4VideoQuality = arrayOf(
                "2", "6", "12", "18", "26"
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_convert_mp4, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerVideoQuality.setSelection(1)
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        onSuccess(Mp4CmdConfig(inputFiles, mp4VideoQuality[spinnerVideoQuality.selectedItemPosition]))
    }
}

class Mp4CmdConfig(
        inputFiles: List<String>,
        private val videoQuality: String
) : CommandConfig(inputFiles) {

    override fun getNumberOfOutput(): Int = inputFiles.size // 1 input - 1 output

    override fun generateOutputFileNames(): List<String> {
        return inputFiles.map {
            val inputFileName = it.parseInputUri().lastPathSegment?.trim()
            if (inputFileName == null || inputFileName.isEmpty()) {
                return@map "Untitled.mp4"
            }
            val (name, extension) = inputFileName.parseFileName()
            Timber.d("File '$name' ext '$extension'")
            return@map "$name.mp4"
        }
    }

    override fun makeJobs(finalOutputs: List<Output>): List<Job> {
        check(finalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map_metadata 0:g -map 0:v -map '0:a?' -map '0:s?' -c:v mpeg4 -c:a aac -c:s srt ")
                .append("-q:v $videoQuality")
                .toString()
        return finalOutputs.mapIndexed { index, output ->
            Job(
                    title = output.title,
                    command = Command(
                            listOf(inputFiles[index]), output.outputUri, // single input output
                            Muxer.MP4, cmdArgs, emptyMap()
                    )
            )
        }
    }

}