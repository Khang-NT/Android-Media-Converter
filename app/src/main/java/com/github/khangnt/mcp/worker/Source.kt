package com.github.khangnt.mcp.worker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import com.github.khangnt.mcp.DEFAULT_CONNECTION_TIMEOUT
import com.github.khangnt.mcp.DEFAULT_IO_TIMEOUT
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.exception.HttpResponseCodeException
import com.github.khangnt.mcp.util.closeQuietly
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

object Sources {
    fun from(inputStream: InputStream): SourceInputStream {
        return object : SourceInputStream {
            override fun openInputStream(): InputStream = inputStream

            override fun close() {
                inputStream.closeQuietly()
            }
        }
    }
}

interface SourceInputStream : Closeable {
    fun openInputStream(): InputStream
}

interface SourceOutputStream : Closeable {
    fun openOutputStream(): OutputStream
}

class ContentResolverSource(
        context: Context,
        private val uri: Uri
) : SourceInputStream, SourceOutputStream {

    private val contentResolver: ContentResolver = context.applicationContext.contentResolver

    init {
        if (uri.scheme != ContentResolver.SCHEME_CONTENT
                && uri.scheme != ContentResolver.SCHEME_FILE) {
            throw IllegalArgumentException("Only accept scheme '${ContentResolver.SCHEME_CONTENT}'" +
                    "and '${ContentResolver.SCHEME_FILE}' but found ${uri.scheme}")
        }
    }

    override fun openInputStream(): InputStream = BufferedInputStream(contentResolver.openInputStream(uri))

    override fun openOutputStream(): OutputStream = BufferedOutputStream(contentResolver.openOutputStream(uri))

    override fun close() {
        // does nothing
    }

}

class SocketSourceOutput(
        address: InetSocketAddress,
        timeout: Int = DEFAULT_CONNECTION_TIMEOUT,
        soTimeout: Int = DEFAULT_IO_TIMEOUT
) : SourceOutputStream {
    private val socket = Socket()
    private val outputStream: OutputStream by lazy {
        socket.soTimeout = soTimeout
        socket.connect(address, timeout)
        socket.getOutputStream()
    }

    override fun openOutputStream(): OutputStream = outputStream

    override fun close() {
        socket.closeQuietly()
    }
}

class HttpSourceInput(
        context: Context,
        private val request: Request,
        private val okHttpClient: OkHttpClient = SingletonInstances.getOkHttpClient()
): SourceInputStream {
    constructor(context: Context, url: String) : this(context, Request.Builder().url(url).build())

    private val wifiWakeLock: WifiManager.WifiLock

    init {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiWakeLock = wifiManager.createWifiLock("ConverterWifiLock")
    }

    private var requestCalled: Boolean = false
    private val response by lazy {
        requestCalled = true
        acquireWifi()
        okHttpClient.newCall(request).execute().apply {
            if (this.code() / 100 != 2) {
                releaseWifi()
                // because this request failed, try get error body and close this Response object
                this.use {
                    throw HttpResponseCodeException(this.code(), this.message(),
                            this.body()?.string() ?: "")
                }
            }
        }
    }

    override fun openInputStream(): InputStream = object : BufferedInputStream(response.body()!!.byteStream()) {
        override fun close() {
            releaseWifi()
            super.close()
        }
    }

    override fun close() {
        releaseWifi()
        if (requestCalled) {
            response.closeQuietly()
        }
    }

    private fun acquireWifi() {
        if (!wifiWakeLock.isHeld) wifiWakeLock.acquire()
    }

    private fun releaseWifi() {
        if (wifiWakeLock.isHeld) wifiWakeLock.release()
    }

}
