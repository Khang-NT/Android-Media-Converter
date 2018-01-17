package com.github.khangnt.mcp.worker

import android.content.Context
import android.net.Uri
import com.github.khangnt.mcp.FFMPEG_TEMP_OUTPUT_FILE
import com.github.khangnt.mcp.exception.FFmpegBinaryPrepareException
import com.github.khangnt.mcp.job.Command
import com.github.khangnt.mcp.util.catchAll
import timber.log.Timber
import java.io.File
import java.lang.StringBuilder
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.util.*

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

data class TcpInput(val sourceInput: SourceInputStream, val address: InetSocketAddress) {
    companion object {
        fun findFreeServerAddress(): InetSocketAddress {
            val serverSocket = ServerSocket(0)
            val address = InetSocketAddress("0.0.0.0", serverSocket.localPort)
            catchAll { serverSocket.close() }
            return address
        }
    }
}

data class CommandResolver(
        val command: Command,
        val execCommand: String,
        val tcpInputs: List<TcpInput>,
        val sourceOutput: SourceOutputStream,
        val tempFile: File,
        val tempFileSourceInput: SourceInputStream
) {
    companion object {
        fun resolve(
                context: Context,
                command: Command,
                ffmpegPath: File
        ): CommandResolver {

            val ffmpegPathResolved = FFmpegPathResolver.resolvePath(context, ffmpegPath)
            val tcpInputs = mutableListOf<TcpInput>()
            val execCommandBuilder = StringBuilder(ffmpegPathResolved.toString())

            command.inputs.forEach { input ->
                execCommandBuilder.append(" -i")
                val uri = Uri.parse(input)
                when (uri.scheme.toLowerCase()) {
                    "file" -> execCommandBuilder.append(" '$uri'")
                    "content" -> {
                        val freeServerAddress = TcpInput.findFreeServerAddress()
                        execCommandBuilder.append(" '${getTcpUri(freeServerAddress)}'")
                        tcpInputs.add(TcpInput(ContentResolverSource(context, uri), freeServerAddress))
                    }
                    "http", "https" -> {
                        val freeServerAddress = TcpInput.findFreeServerAddress()
                        execCommandBuilder.append(" '${getTcpUri(freeServerAddress)}'")
                        tcpInputs.add(TcpInput(HttpSourceInput(context, uri.toString()), freeServerAddress))
                    }
                    else -> {
                        throw IllegalArgumentException("Can't resolve input $input")
                    }
                }
            }

            execCommandBuilder.append(" ").append(command.args)

            val sourceOutput = ContentResolverSource(context, Uri.parse(command.output))

            // temp file to save ffmpeg output
            val tempFile = try {
                val file = File(context.getExternalFilesDir(null), FFMPEG_TEMP_OUTPUT_FILE)
                if (file.parentFile.canWrite()) {
                    file
                } else {
                    File(context.filesDir, FFMPEG_TEMP_OUTPUT_FILE)
                }
            } catch (all: Exception) {
                File(context.filesDir, FFMPEG_TEMP_OUTPUT_FILE)
            }

            if (!tempFile.parentFile.exists()) {
                val res = if (tempFile.parentFile.mkdirs()) "success" else "failed"
                Timber.d("Create parent dir $res: $tempFile")
            }

            val tempOutputUri = Uri.fromFile(tempFile)
            val tempSourceInput = ContentResolverSource(context, tempOutputUri)
            execCommandBuilder.append(" -y -f ${command.outputFormat} '$tempOutputUri'")

            return CommandResolver(command, execCommandBuilder.toString(),
                    Collections.unmodifiableList(tcpInputs), sourceOutput,
                    tempFile, tempSourceInput)
        }

        private fun getTcpUri(address: InetSocketAddress): String {
            return "tcp://${address.hostName}:${address.port}" +
                    "?listen=1"
        }
    }

}

object FFmpegPathResolver {
    private val globalLock = Any()

    /**
     * Ensure FFmpeg file has executable permission.
     */
    @Throws(FFmpegBinaryPrepareException::class)
    fun resolvePath(context: Context, originalPath: File): File {
        if (originalPath.canExecute() ||
                catchAll(printLog = true) { originalPath.setExecutable(true) } == true) {
            return originalPath
        }
        synchronized(globalLock) {
            val copyTo = File(context.filesDir, originalPath.name)
            if (!copyTo.exists() || copyTo.length() != originalPath.length()) {
                Timber.d("Start copying FFmpeg to: $copyTo")
                CopierThread(
                        ContentResolverSource(context, Uri.fromFile(originalPath)),
                        ContentResolverSource(context, Uri.fromFile(copyTo)),
                        onError = {
                            throw FFmpegBinaryPrepareException("Copy FFmpeg binary failed: $it", it)
                        },
                        onSuccess = {
                            Timber.d("Successfully copy FFmpeg binary to: $copyTo")
                        }
                ).run()
            }
            if (copyTo.canExecute() ||
                    catchAll(printLog = true) { copyTo.setExecutable(true) } == true) {
                Timber.d("Grant executable permission success on $copyTo")
                return copyTo
            } else {
                throw FFmpegBinaryPrepareException("Can't grant executable permission on: $copyTo", null)
            }
        }
    }

}