package com.github.khangnt.mcp

import android.content.Context
import com.github.khangnt.mcp.exception.HttpResponseCodeException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.liulishuo.filedownloader.exception.FileDownloadHttpException
import com.liulishuo.filedownloader.exception.FileDownloadOutOfSpaceException
import java.io.EOFException
import java.io.InterruptedIOException
import java.net.HttpRetryException
import java.net.ProtocolException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Created by Khang NT on 1/13/18.
 * Email: khang.neon.1997@gmail.com
 */

fun <T : Throwable> rootCauseIs(clazz: Class<T>, error: Throwable): Boolean {
    var temp: Throwable? = error
    while (temp !== null) {
        if (clazz.isInstance(temp)) {
            return true
        }
        temp = temp.cause
    }
    return false
}

fun <T : Throwable> Throwable.castTo(clazz: Class<T>): T? {
    var temp: Throwable? = this
    while (temp !== null) {
        if (clazz.isInstance(temp)) {
            return clazz.cast(temp)
        }
        temp = temp.cause
    }
    throw ClassCastException("Can't cast $this to $clazz")
}

private fun inWhiteList(error: Throwable): Boolean =
        error.javaClass == Exception::javaClass ||  // dumb error
                rootCauseIs(InterruptedException::class.java, error) ||
                rootCauseIs(InterruptedIOException::class.java, error) ||
                rootCauseIs(UnknownHostException::class.java, error) ||
                rootCauseIs(SSLException::class.java, error) ||
                rootCauseIs(HttpResponseCodeException::class.java, error) ||
                rootCauseIs(SocketException::class.java, error) ||
                rootCauseIs(EOFException::class.java, error) ||
                rootCauseIs(FileDownloadHttpException::class.java, error) ||
                rootCauseIs(HttpRetryException::class.java, error) ||
                rootCauseIs(ProtocolException::class.java, error) ||
                error.message?.contains("ENOSPC") == true || // No space left on device
                rootCauseIs(FileDownloadOutOfSpaceException::class.java, error)


fun reportNonFatal(throwable: Throwable, where: String, message: String? = null) {
    if (!BuildConfig.DEBUG && !inWhiteList(throwable)) {
        FirebaseCrashlytics.getInstance().setCustomKey("where", "Non-fatal at '$where': ${message ?: throwable.message}")
        FirebaseCrashlytics.getInstance().log(message ?: throwable.message.toString())
    }
}

fun getKnownReasonOf(error: Throwable, context: Context, fallback: String): String {
    if (rootCauseIs(UnknownHostException::class.java, error) ||
            rootCauseIs(SSLException::class.java, error) ||
            rootCauseIs(SocketException::class.java, error) ||
            error.message?.contains("unexpected end of stream", ignoreCase = true) == true ||
            rootCauseIs(ProtocolException::class.java, error) ||
            rootCauseIs(HttpRetryException::class.java, error)) {
        return context.getString(R.string.network_error)
    } else if (rootCauseIs(FileDownloadHttpException::class.java, error)) {
        val httpException = error.castTo(FileDownloadHttpException::class.java)
        if (httpException != null) {
            return "Link broken, response: ${httpException.code}"
        }
    } else if (rootCauseIs(HttpResponseCodeException::class.java, error)) {
        val httpResponseCodeException = error.castTo(HttpResponseCodeException::class.java)
        if (httpResponseCodeException != null) {
            return "Link broken, response: ${httpResponseCodeException.message}"
        }
    } else if (error.message?.contains("ENOSPC") == true ||
            rootCauseIs(FileDownloadOutOfSpaceException::class.java, error)) {
        return "Your device's storage is full"
    }
    return fallback
}