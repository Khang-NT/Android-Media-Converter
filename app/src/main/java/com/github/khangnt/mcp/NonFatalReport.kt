package com.github.khangnt.mcp

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.github.khangnt.mcp.exception.HttpResponseCodeException
import java.io.EOFException
import java.io.InterruptedIOException
import java.lang.ClassCastException
import java.lang.Exception
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

fun <T : Throwable> Throwable.castTo(clazz: Class<T>): T {
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
                rootCauseIs(EOFException::class.java, error)


fun reportNonFatal(throwable: Throwable, where: String, message: String? = null) {
    if (!BuildConfig.DEBUG && !inWhiteList(throwable)) {
        Crashlytics.setString("where", "Non-fatal at '$where': ${message ?: throwable.message}")
        Crashlytics.logException(throwable)
    }
}

fun getKnownReasonOf(error: Throwable, context: Context, fallback: String): String {
    if (rootCauseIs(UnknownHostException::class.java, error) ||
            rootCauseIs(SSLException::class.java, error) ||
            rootCauseIs(SocketException::class.java, error) ||
            error.message?.contains("unexpected end of stream", ignoreCase = true) == true) {
        return context.getString(R.string.network_error)
    } else if (rootCauseIs(HttpResponseCodeException::class.java, error)) {
        val httpResponseCodeException = error.castTo(HttpResponseCodeException::class.java)
        return "Http response: ${httpResponseCodeException.message}"
    }
    return fallback
}