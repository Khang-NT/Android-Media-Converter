package com.github.khangnt.mcp.util

import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.exception.HttpResponseCodeException
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Khang NT on 1/3/18.
 * Email: khang.neon.1997@gmail.com
 */

fun String.get(): Single<String> {
    val request = Request.Builder().url(this).get().build()
    val call = SingletonInstances.getOkHttpClient().newCall(request)
    return Single.create<String>({ emitter ->
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.d(e, "Get failed [$this@get]")
                emitter.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code() / 100 == 2) {
                    response.use { emitter.onSuccess(response.body()!!.string()) }
                } else {
                    emitter.onError(HttpResponseCodeException(
                            code = response.code(), statusMessage = response.message(),
                            errorBody = response.body()?.string() ?: ""
                    ))
                }
            }

        })
    })      // force subscribe synchronously, because call will be enqueued immediately
            .subscribeOn(Schedulers.trampoline())
}

fun downloadFile(url: String, path: File): Flowable<Long> {
    val request = Request.Builder().url(url).get().build()
    val call = SingletonInstances.getOkHttpClient().newCall(request)
    return Flowable.create({ emitter ->
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Timber.d(e, "Download file failed: [$url] to [$path]")
                emitter.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    var total: Long = 0
                    try {
                        FileOutputStream(path).use { fileOs ->
                            response.body()!!.byteStream().use { respIs ->
                                copy(respIs, fileOs) {
                                    total += it
                                    emitter.onNext(total)
                                }
                            }
                        }
                        emitter.onComplete()
                    } catch (error: Throwable) {
                        emitter.onError(error)
                    }
                }
            }
        })
    }, BackpressureStrategy.LATEST)
}