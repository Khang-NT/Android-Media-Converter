package com.github.khangnt.mcp.worker

import android.content.Context
import android.net.Uri
import com.github.khangnt.mcp.FFMPEG_TEMP_OUTPUT_FILE
import com.github.khangnt.mcp.job.Command
import com.github.khangnt.mcp.util.catchAll
import java.io.File
import java.lang.StringBuilder
import java.net.InetAddress
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
            val address = InetSocketAddress(InetAddress.getLocalHost(), serverSocket.localPort)
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
            val tcpInputs = mutableListOf<TcpInput>()
            val execCommandBuilder = StringBuilder(ffmpegPath.toString())
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
                        tcpInputs.add(TcpInput(HttpSourceInput(uri.toString()), freeServerAddress))
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
                val file = File(context.externalCacheDir, FFMPEG_TEMP_OUTPUT_FILE)
                if (file.parentFile.canWrite()) {
                    file
                } else {
                    File(context.cacheDir, FFMPEG_TEMP_OUTPUT_FILE)
                }
            } catch (all: Exception) {
                File(context.cacheDir, FFMPEG_TEMP_OUTPUT_FILE)
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