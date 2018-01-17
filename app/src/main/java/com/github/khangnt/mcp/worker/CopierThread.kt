package com.github.khangnt.mcp.worker

import com.github.khangnt.mcp.DEFAULT_IO_BUFFER_LENGTH
import com.github.khangnt.mcp.util.closeQuietly
import com.github.khangnt.mcp.util.copy
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

class CopierThread(
        private val sourceInput: SourceInputStream,
        private val sourceOutput: SourceOutputStream,
        private val bufferLength: Int = DEFAULT_IO_BUFFER_LENGTH,
        private val onError: (Throwable) -> Unit,
        private val onSuccess: () -> Unit = {}
) : Thread() {

    override fun run() {
        var success = false
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            input = sourceInput.openInputStream()
            output = sourceOutput.openOutputStream()
            copy(input, output, bufferLength)
            success = true
        } catch (anyError: Throwable) {
            onError(anyError)
        } finally {
            // close sources
            input.closeQuietly()
            output.closeQuietly()
            sourceInput.closeQuietly()
            sourceOutput.closeQuietly()
        }
        if (success) onSuccess.invoke()
    }

}