package com.github.khangnt.mcp

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import timber.log.Timber

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this)

        SingletonInstances.init(this)

        Fabric.with(this, Crashlytics())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}