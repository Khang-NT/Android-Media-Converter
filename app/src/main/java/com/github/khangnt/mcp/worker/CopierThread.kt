package com.github.khangnt.mcp.worker

import com.github.khangnt.mcp.DEFAULT_IO_BUFFER_LENGTH
import com.github.khangnt.mcp.util.closeQuietly
import com.github.khangnt.mcp.util.copy

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

class CopierThread(
        private val sourceInput: SourceInputStream,
        private val sourceOutput: SourceOutputStream,
        private val bufferLength: Int = DEFAULT_IO_BUFFER_LENGTH,
        private val onCopied: (Int) -> Unit,
        private val onError: (Throwable) -> Unit,
        private val onSuccess: () -> Unit = {}
) : Thread() {

    override fun run() {
        try {
            sourceInput.openInputStream().use { input ->
                sourceOutput.openOutputStream().use { output ->
                    copy(input, output, bufferLength, onCopied)
                    onSuccess.invoke()
                }
            }
        } catch (anyError: Throwable) {
            onError(anyError)
        } finally {
            // close sources
            sourceInput.closeQuietly()
            sourceOutput.closeQuietly()
        }
    }

}