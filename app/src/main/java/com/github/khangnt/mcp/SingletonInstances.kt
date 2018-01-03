package com.github.khangnt.mcp

import android.content.Context
import com.github.khangnt.mcp.db.JobDb
import com.github.khangnt.mcp.db.MainSqliteOpenHelper
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

class SingletonInstances(val appContext: Context) {
    companion object {
        const val CACHE_SIZE = 20 * 1024 * 1024L     //20MB
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
                .addInterceptor(HttpLoggingInterceptor({ Timber.d(it) }).setLevel(BODY))
                .build()
    }

    private val mainSqliteOpenHelperLazy by lazy { MainSqliteOpenHelper(appContext) }
    private val jobDatabaseLazy by lazy { JobDb(mainSqliteOpenHelperLazy) }

    fun getOkHttpClient(): OkHttpClient = okHttpClientLazy

    fun getJobDatabase(): JobDb = jobDatabaseLazy
}