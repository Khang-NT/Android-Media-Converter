package com.github.khangnt.mcp

import android.content.Context
import com.github.khangnt.mcp.db.JobDb
import com.github.khangnt.mcp.db.MainSqliteOpenHelper
import com.github.khangnt.mcp.job.DefaultJobManager
import com.github.khangnt.mcp.job.JobManager
import com.github.khangnt.mcp.ui.prefs.SharedPrefs
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.HEADERS
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

class SingletonInstances private constructor(appContext: Context) {
    companion object {
        private const val CACHE_SIZE = 20 * 1024 * 1024L     //20MB

        private lateinit var INSTANCE: SingletonInstances
        private var initialized = false

        fun init(context: Context) {
            check(!initialized, { "Only init once" })
            INSTANCE = SingletonInstances(context.applicationContext)
            initialized = true
        }

        fun isInitialized() = initialized

        fun getOkHttpClient(): OkHttpClient = INSTANCE.okHttpClientLazy

        fun getJobManager(): JobManager = INSTANCE.jobManagerLazy

        fun getSharedPrefs(): SharedPrefs = INSTANCE.sharedPrefsLazy

    }

    private val mainCacheLazy by lazy {
        Cache(File(appContext.cacheDir, "main_cache"), CACHE_SIZE)
    }

    private val okHttpClientLazy by lazy {
        OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .followSslRedirects(true)
                .cache(mainCacheLazy)
                .connectTimeout(DEFAULT_CONNECTION_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_IO_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_IO_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor({ Timber.d(it) }).setLevel(HEADERS))
                    }
                }
                .build()
    }

    private val mainSqliteOpenHelperLazy by lazy { MainSqliteOpenHelper(appContext) }

    private val jobDatabaseLazy by lazy { JobDb(mainSqliteOpenHelperLazy) }

    private val jobManagerLazy by lazy { DefaultJobManager(jobDatabaseLazy) }

    private val sharedPrefsLazy by lazy { SharedPrefs(appContext) }
}