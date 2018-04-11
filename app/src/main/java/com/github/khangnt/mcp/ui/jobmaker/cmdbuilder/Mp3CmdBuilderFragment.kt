package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.annotation.Encoders
import com.github.khangnt.mcp.annotation.Mp3Encoder
import com.github.khangnt.mcp.annotation.Muxer
import com.github.khangnt.mcp.annotation.QualityType
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.db.job.Job
import com.github.khangnt.mcp.util.parseFileName
import com.github.khangnt.mcp.util.parseInputUri
import kotlinx.android.synthetic.main.fragment_todo.*
import timber.log.Timber
import java.lang.IllegalStateException

/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */

class Mp3CmdBuilderFragment : CommandBuilderFragment() {

    companion object {
        fun create(inputFiles: List<String>) = Mp3CmdBuilderFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(ARG_INPUT_FILES, ArrayList(inputFiles))
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_todo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView.text = """- Create UI to configure command""".trimIndent()
    }

    override fun validateConfig(onSuccess: (CommandConfig) -> Unit) {
        onSuccess(Mp3CmdConfig(inputFiles, Encoders.LIBMP3LAME, QualityType.CBR, 320))
    }
}

class Mp3CmdConfig(
        inputFiles: List<String>,
        @Mp3Encoder private val encoder: String,
        @QualityType private val qualityType: Int,
        private val quality: Int
) : CommandConfig(inputFiles) {

    init {
        check(encoder == Encoders.LIBMP3LAME || encoder == Encoders.LIBSHINE) {
            "Invalid mp3 encoder: $encoder"
        }
    }

    override fun getNumberOfOutput(): Int = inputFiles.size // 1 input - 1 output

    override fun generateOutputFileNames(): List<String> {
        return inputFiles.map {
            val inputFileName = it.parseInputUri().lastPathSegment?.trim()
            if (inputFileName == null || inputFileName.isEmpty()) {
                return@map "Untitled.mp3"
            }
            val (name, extension) = inputFileName.parseFileName()
            Timber.d("File '$name' ext '$extension'")
            return@map "$name.mp3"
        }
    }

    override fun makeJobs(finalOutputs: List<Output>): List<Job> {
        check(finalOutputs.size == getNumberOfOutput())
        val cmdArgs = StringBuffer("-hide_banner -map 0:a -map_metadata 0:g -codec:a ")
                .append("-codec:a $encoder ")
                .append(when (qualityType) {
                    QualityType.CBR -> "-b:a ${quality}k "
                    QualityType.VBR -> "-q:a $quality "
                    else -> throw IllegalStateException("Unknown quality type: $qualityType")
                })
                .toString()
        return finalOutputs.mapIndexed { index, output ->
            Job(
                    title = output.title,
                    command = Command(
                            listOf(inputFiles[index]), output.outputUri, // single input output
                            Muxer.MP3, cmdArgs, emptyMap()
                    )
            )
        }
    }

}