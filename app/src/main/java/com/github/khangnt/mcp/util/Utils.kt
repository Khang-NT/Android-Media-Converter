package com.github.khangnt.mcp.util

import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
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
        if (printLog) Timber.d(ignore)
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