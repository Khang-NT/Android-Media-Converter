package com.github.khangnt.mcp

import android.app.Application
import android.os.StrictMode
import com.crashlytics.android.Crashlytics
import com.github.khangnt.mcp.util.IMMLeaks
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.database.NoDatabaseImpl
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
        setupStrictMode()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        IMMLeaks.fixFocusedViewLeak(this)

        SingletonInstances.init(this)

        FileDownloader.setupOnApplicationOnCreate(this)
                .database { NoDatabaseImpl() }
                .maxNetworkThreadCount(4)
                .commit()

        Fabric.with(this, Crashlytics())

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyFlashScreen()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build())
        } else {
            // on release, don't detect any thing
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().penaltyLog().build())
        }
    }
}