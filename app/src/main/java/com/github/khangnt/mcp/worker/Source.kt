package com.github.khangnt.mcp.worker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.github.khangnt.mcp.DEFAULT_CONNECTION_TIMEOUT
import com.github.khangnt.mcp.HttpResponseCodeException
import com.github.khangnt.mcp.MainApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

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
                    "and '${ContentResolver.SCHEME_FILE}'")
        }
    }

    override fun openInputStream(): InputStream = contentResolver.openInputStream(uri)

    override fun openOutputStream(): OutputStream = contentResolver.openOutputStream(uri)

    override fun close() {
        // does nothing
    }

}

class SocketSourceOutput(
        address: InetSocketAddress,
        timeout: Int = DEFAULT_CONNECTION_TIMEOUT
) : SourceOutputStream {
    private val socket = Socket()
    private val outputStream: OutputStream by lazy {
        socket.connect(address, timeout)
        socket.getOutputStream()
    }

    override fun openOutputStream(): OutputStream = outputStream

    override fun close() {
        socket.close()
    }
}

class HttpSourceInput(
        val request: Request,
        private val okHttpClient: OkHttpClient = MainApplication.singletonInstances.getOkHttpClient()
): SourceInputStream {
    constructor(url: String) : this(Request.Builder().url(url).build())

    private var requestCalled: Boolean = false
    private val response by lazy {
        requestCalled = true
        okHttpClient.newCall(request).execute().apply {
            if (this.code() / 100 != 2) {
                // because this request failed, try get error body and close this Response object
                this.use {
                    throw HttpResponseCodeException(this.code(), this.message(),
                            this.body()?.string() ?: "")
                }
            }
        }
    }

    override fun openInputStream(): InputStream = response.body()!!.byteStream()

    override fun close() {
        if (requestCalled) {
            response.close()
        }
    }

}

