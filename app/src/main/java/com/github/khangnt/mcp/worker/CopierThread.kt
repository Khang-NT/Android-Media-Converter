package com.github.khangnt.mcp.worker

import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.copyAndClose

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

class CopierThread(
        private val sourceInput: SourceInputStream,
        private val sourceOutput: SourceOutputStream,
        private val bufferSize: Int,
        private val progress: (Int) -> Unit,
        private val onError: (Throwable) -> Unit
): Thread() {

    override fun run() {
        try {
            copyAndClose(sourceInput.openInputStream(), sourceOutput.openOutputStream(),
                    ByteArray(bufferSize), progress)
        } catch (anyError: Throwable) {
            onError(anyError)
        } finally {
            // ignore error while closing sources
            catchAll { sourceInput.close() }
            catchAll { sourceOutput.close() }
        }
    }

}