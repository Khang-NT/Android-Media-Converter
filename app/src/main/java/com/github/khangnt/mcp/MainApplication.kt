package com.github.khangnt.mcp

import android.os.StrictMode
import androidx.multidex.MultiDexApplication
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.crashlytics.android.Crashlytics
import com.github.khangnt.mcp.util.IMMLeaks
import com.google.android.gms.ads.MobileAds
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.database.NoDatabaseImpl
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

class MainApplication : MultiDexApplication() {

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

        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                .showErrorDetails(true)
                .showRestartButton(true)
                .trackActivities(true)
                .apply()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }

        setUpRxPlugins()

        MobileAds.initialize(this)

        val sharedPrefs = SingletonInstances.getSharedPrefs()
        if (sharedPrefs.conversionCountLeftBeforeShowAds <= 0) {
            sharedPrefs.enabledAds = true
            sharedPrefs.conversionCountLeftBeforeShowAds = 0
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

    private fun setUpRxPlugins() {
        RxJavaPlugins.setErrorHandler { throwable ->
            Timber.d(throwable)
            if (throwable is UndeliverableException) {
                reportNonFatal(throwable.cause!!, "rx_undeliverable_exception")
            } else {
                reportNonFatal(throwable, "rx_undeliverable_exception")
            }
        }
    }
}