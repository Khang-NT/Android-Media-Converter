package com.github.khangnt.mcp.worker

import android.content.Context
import android.net.Uri
import com.github.khangnt.mcp.FFMPEG_FILE
import com.github.khangnt.mcp.FFMPEG_TEMP_OUTPUT_FILE
import com.github.khangnt.mcp.exception.FFmpegBinaryPrepareException
import com.github.khangnt.mcp.job.Command
import com.github.khangnt.mcp.util.catchAll
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.net.InetAddress
import java.net.ServerSocket
import java.util.*

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

data class ServerSocketInput(
        val sourceInput: SourceInputStream,
        val serverSocket: ServerSocket = createServerSocket()
) {
    companion object {
        fun createServerSocket(): ServerSocket {
            return try {
                ServerSocket(0, 0, InetAddress.getByName("0.0.0.0"))
            } catch (error: Throwable) {
                throw IOException("Can't create socket: ${error.message}", error)
            }
        }
    }
}

data class CommandResolver(
        val command: Command,
        val execCommand: String,
        val serverSocketInputs: List<ServerSocketInput>,
        val sourceOutput: SourceOutputStream,
        val tempFile: File,
        val tempFileSourceInput: SourceInputStream
) {
    companion object {
        fun resolve(
                context: Context,
                command: Command
        ): CommandResolver {

            val ffmpegPathResolved = FFmpegPathResolver.resolvePath(context)
            val tcpInputs = mutableListOf<ServerSocketInput>()
            val execCommandBuilder = StringBuilder(ffmpegPathResolved.toString())

            command.inputs.forEach { input ->
                execCommandBuilder.append(" -i")
                val uri = Uri.parse(input)
                when (uri.scheme.toLowerCase()) {
                    "file" -> execCommandBuilder.append(" '$uri'")
                    "content" -> {
                        val serverSocketInput = ServerSocketInput(ContentResolverSource(context, uri))
                        execCommandBuilder.append(" '${getTcpUri(serverSocketInput.serverSocket.localPort)}'")
                        tcpInputs.add(serverSocketInput)
                    }
                    "http", "https" -> {
                        val serverSocketInput = ServerSocketInput(HttpSourceInput(context, uri.toString()))
                        execCommandBuilder.append(" '${getTcpUri(serverSocketInput.serverSocket.localPort)}'")
                        tcpInputs.add(serverSocketInput)
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

        private fun getTcpUri(port: Int): String {
            return "tcp://0.0.0.0:${port}" +
                    "?listen=0"
        }
    }

}

object FFmpegPathResolver {
    private val globalLock = Any()

    /**
     * Ensure FFmpeg file has executable permission.
     */
    @Throws(FFmpegBinaryPrepareException::class)
    fun resolvePath(context: Context): File {
        val originalPath = File(context.applicationInfo.nativeLibraryDir, FFMPEG_FILE)
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