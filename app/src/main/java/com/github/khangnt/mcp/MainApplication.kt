package com.github.khangnt.mcp

import android.app.Application
import android.content.IntentFilter
import com.crashlytics.android.Crashlytics
import com.github.khangnt.mcp.receiver.RateDialogBroadcastReceiver
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import timber.log.Timber

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

const val SHOW_RATE_DIALOG_ACTION = BuildConfig.APPLICATION_ID + ".show_rate_dialog"

class MainApplication: Application() {

    val rateDialogBroadcastReceiver = RateDialogBroadcastReceiver()
    val intentFilter = IntentFilter(SHOW_RATE_DIALOG_ACTION)

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        SingletonInstances.init(this)

        Fabric.with(this, Crashlytics())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(rateDialogBroadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(rateDialogBroadcastReceiver)
    }

}