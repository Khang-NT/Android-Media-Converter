package com.github.khangnt.mcp

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProvider
import android.arch.persistence.room.Room
import android.content.Context
import com.github.khangnt.mcp.db.DB_NAME
import com.github.khangnt.mcp.db.MainDatabase
import com.github.khangnt.mcp.db.Migration1To2
import com.github.khangnt.mcp.db.job.DefaultJobRepository
import com.github.khangnt.mcp.db.job.JobRepository
import com.github.khangnt.mcp.ui.prefs.SharedPrefs
import com.github.khangnt.mcp.worker.JobWorkerManager
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

class SingletonInstances private constructor(private val appContext: Context) {
    companion object {
        private const val CACHE_SIZE = 20 * 1024 * 1024L     //20MB

        @SuppressLint("StaticFieldLeak")
        private lateinit var INSTANCE: SingletonInstances
        private var initialized = false

        fun init(context: Context) {
            check(!initialized, { "Only init once" })
            INSTANCE = SingletonInstances(context.applicationContext)
            initialized = true
        }

        fun isInitialized() = initialized

        fun getOkHttpClient(): OkHttpClient = INSTANCE.okHttpClientLazy

        fun getJobRepository(): JobRepository = INSTANCE.jobRepositoryLazy

        fun getSharedPrefs(): SharedPrefs = INSTANCE.sharedPrefsLazy

        fun getViewModelFactory(): ViewModelProvider.Factory = INSTANCE.viewModelFactory

        fun getJobWorkerManager(): JobWorkerManager = INSTANCE.jobWorkerManagerLazy

        fun getAppContext() = INSTANCE.appContext
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

    private val mainDatabase = Room.databaseBuilder(appContext, MainDatabase::class.java, DB_NAME)
            .apply { if (!BuildConfig.DEBUG) fallbackToDestructiveMigration() }
            .addMigrations(Migration1To2())
            .build()

    private val jobRepositoryLazy by lazy { DefaultJobRepository(mainDatabase.getJobDao()) }

    private val sharedPrefsLazy by lazy { SharedPrefs(appContext) }

    private val viewModelFactory = ViewModelFactory(appContext)

    private val jobWorkerManagerLazy: JobWorkerManager
            by lazy { JobWorkerManager(appContext, jobRepositoryLazy, sharedPrefsLazy) }

}