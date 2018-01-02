package com.github.khangnt.mcp

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */


inline fun catchAll(printLog: Boolean = false, action: () -> Unit) {
    try {
        action()
    } catch (ignore: Throwable) {
        if (printLog) ignore.printStackTrace()
    }
}

inline fun copyAndClose(input: InputStream, output: OutputStream, buffer: ByteArray, progress: (Int) -> Unit) {
    var readLength = 0
    input.use {
        output.use {
            while (input.read(buffer).apply { readLength = this } > 0) {
                output.write(buffer, 0, readLength)
                progress(readLength)
            }
        }
    }
}