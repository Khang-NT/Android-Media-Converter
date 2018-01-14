package com.github.khangnt.mcp

import android.content.Context
import com.crashlytics.android.Crashlytics
import java.io.InterruptedIOException
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

private fun inWhiteList(error: Throwable): Boolean =
        rootCauseIs(InterruptedException::class.java, error) ||
                rootCauseIs(InterruptedIOException::class.java, error) ||
                rootCauseIs(UnknownHostException::class.java, error) ||
                SSLException::class.java == error.javaClass

fun reportNonFatal(throwable: Throwable, where: String, message: String? = null) {
    if (!BuildConfig.DEBUG && !inWhiteList(throwable)) {
        Crashlytics.setString("where", "Non-fatal at '$where': ${message ?: throwable.message}")
        Crashlytics.logException(throwable)
    }
}

fun getKnownReasonOf(error: Throwable, context: Context, fallback: String): String {
    if (rootCauseIs(UnknownHostException::class.java, error) ||
            SSLException::class.java == error.javaClass) {
        return context.getString(R.string.error_no_internet)
    }
    return fallback
}