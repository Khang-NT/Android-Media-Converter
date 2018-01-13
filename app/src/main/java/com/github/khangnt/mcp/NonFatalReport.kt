package com.github.khangnt.mcp

import com.crashlytics.android.Crashlytics
import java.io.InterruptedIOException

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
                rootCauseIs(InterruptedIOException::class.java, error)

fun reportNonFatal(throwable: Throwable, where: String, message: String? = null) {
    if (!inWhiteList(throwable)) {
        Crashlytics.setString("where", "Non-fatal at '$where': ${message ?: throwable.message}")
        Crashlytics.logException(throwable)
    }
}