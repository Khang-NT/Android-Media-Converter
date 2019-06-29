package com.github.khangnt.mcp.worker

import android.content.Context
import android.net.Uri
import com.github.khangnt.mcp.FFMPEG_FILE
import com.github.khangnt.mcp.FFMPEG_SIZE_FILE
import com.github.khangnt.mcp.FFMPEG_TEMP_OUTPUT_FILE
import com.github.khangnt.mcp.db.job.Command
import com.github.khangnt.mcp.exception.FFmpegBinaryPrepareException
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.escapeSingleQuote
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

data class CommandResolver(
        val command: Command,
        val execCommand: String,
        val sourceOutput: SourceOutputStream,
        val tempFile: File,
        val tempFileSourceInput: SourceInputStream
) {
    companion object {
        @Throws(FileNotFoundException::class)
        fun resolve(
                context: Context,
                jobTempDir: File,
                ffmpegPath: File,
                command: Command
        ): CommandResolver {

            val ffmpegPathResolved = FFmpegPathResolver.resolvePath(context, ffmpegPath)
            val execCommandBuilder = StringBuilder(ffmpegPathResolved.toString())

            command.inputs.forEachIndexed { index, input ->
                execCommandBuilder.append(" -i")
                val uri = Uri.parse(input)
                when (uri.scheme?.toLowerCase()) {
                    "file" -> execCommandBuilder.append(" '${formatFileUri(uri)}'")
                    "content", "http", "https" -> {
                        val preparedInput = makeInputTempFile(jobTempDir, index)
                        if (!preparedInput.exists()) {
                            throw FileNotFoundException("Some app data were deleted or moved.\n$preparedInput")
                        }
                        execCommandBuilder.append(" '${formatFileUri(Uri.fromFile(preparedInput))}'")
                    }
                    else -> {
                        throw IllegalArgumentException("Can't resolve input $input")
                    }
                }
            }

            execCommandBuilder.append(" ").append(command.args)

            val finalOutputUri = Uri.parse(command.output)
            if (finalOutputUri.scheme == "file") {
                val outputFolder = File(finalOutputUri.path).parentFile
                if (!outputFolder.exists() && !outputFolder.mkdirs()) {
                    throw FileNotFoundException("Output folder not found: $outputFolder")
                }
            }
            val sourceOutput = ContentResolverSource(context, finalOutputUri)

            // temp file to save ffmpeg output
            val tempFile = File(jobTempDir, FFMPEG_TEMP_OUTPUT_FILE)
            val tempOutputUri = Uri.fromFile(tempFile)
            val tempSourceInput = ContentResolverSource(context, tempOutputUri)
            execCommandBuilder.append(" -y -f ${command.outputFormat} '${formatFileUri(tempOutputUri)}'")

            return CommandResolver(command, execCommandBuilder.toString(),
                    sourceOutput, tempFile, tempSourceInput)
        }

        private fun formatFileUri(uri: Uri): String {
            return "file://${uri.path?.escapeSingleQuote()}"
        }
    }

}

object FFmpegPathResolver {
    private val globalLock = Any()

    /**
     * Copy FFmpeg binary from "assets" if not exists
     */
    @Throws(FFmpegBinaryPrepareException::class)
    fun resolvePath(context: Context, ffmpegPath: File): File {
        synchronized(globalLock) {
            try {
                val assetManager = context.assets
                val ffmpegSize = InputStreamReader(assetManager.open(FFMPEG_SIZE_FILE))
                        .use { it.readText().trim() }
                        .toLongOrNull()
                if (!ffmpegPath.exists() || ffmpegPath.length() != ffmpegSize) {
                    Timber.d("Start copying FFmpeg to: $ffmpegPath")
                    assetManager.open(FFMPEG_FILE).use { inputStream ->
                        val copyTo = context.contentResolver.openOutputStream(Uri.fromFile(ffmpegPath))
                        BufferedOutputStream(copyTo).use { bufferedOutputStream ->
                            inputStream.copyTo(bufferedOutputStream)
                        }
                    }
                    Timber.d("Successfully copy FFmpeg binary to: $ffmpegPath")
                }
            } catch (error: Throwable) {
                throw FFmpegBinaryPrepareException("Copy ffmpeg failed", error)
            }
            if (ffmpegPath.canExecute() ||
                    catchAll(printLog = true) { ffmpegPath.setExecutable(true) } == true) {
                Timber.d("Grant executable permission success on $ffmpegPath")
                return ffmpegPath
            } else {
                throw FFmpegBinaryPrepareException("Can't grant executable permission on: $ffmpegPath", null)
            }
        }
    }

}
