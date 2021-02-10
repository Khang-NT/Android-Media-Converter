package com.github.khangnt.mcp

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Created by Khang NT on 1/16/18.
 * Email: khang.neon.1997@gmail.com
 */

class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        FirebaseCrashlytics.getInstance().log(message)
    }
}