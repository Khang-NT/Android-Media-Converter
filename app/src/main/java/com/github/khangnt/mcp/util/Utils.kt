package com.github.khangnt.mcp.util

import com.github.khangnt.mcp.DEFAULT_IO_BUFFER_LENGTH
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

// close and catch all error, different with .use extension
fun Closeable?.closeQuietly() {
    catchAll { this?.close() }
}

inline fun catchAll(printLog: Boolean = false, action: () -> Unit) {
    try {
        action()
    } catch (ignore: Throwable) {
        if (printLog) Timber.d(ignore)
    }
}

inline fun copy(
        input: InputStream,
        output: OutputStream,
        bufferLength: Int = DEFAULT_IO_BUFFER_LENGTH,
        onCopied: (Int) -> Unit
) {
    val buffer = ByteArray(bufferLength)
    var readLength = 0
    while (input.read(buffer).apply { readLength = this } > 0) {
        output.write(buffer, 0, readLength)
        onCopied(readLength)
    }
}

fun JSONArray.toListString(): List<String> {
    return (0 until this.length()).map { this.getString(it) }
}

fun JSONObject.toMapString(): Map<String, String> {
    val res = mutableMapOf<String, String>()
    this.keys().forEach { res.put(it, this.getString(it)) }
    return res
}

fun <T> Observable<T>.ignoreError(printLog: Boolean = false): Observable<T> =
        this.onErrorResumeNext { error: Throwable ->
            if (printLog) Timber.d(error)
            Observable.empty<T>()
        }