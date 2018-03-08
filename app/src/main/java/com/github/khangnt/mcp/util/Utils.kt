package com.github.khangnt.mcp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.provider.DocumentFile
import com.github.khangnt.mcp.DEFAULT_IO_BUFFER_LENGTH
import com.github.khangnt.mcp.KB
import com.github.khangnt.mcp.MB
import com.github.khangnt.mcp.R
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Khang NT on 1/1/18.
 * Email: khang.neon.1997@gmail.com
 */

// close and catch all error, different with .use extension
fun Closeable?.closeQuietly() {
    catchAll { this?.close() }
}

inline fun <T> catchAll(printLog: Boolean = false, action: () -> T): T? {
    try {
        return action()
    } catch (ignore: Throwable) {
        if (printLog) Timber.d(ignore)
    }
    return null
}

fun copy(
        input: InputStream,
        output: OutputStream,
        bufferLength: Int = DEFAULT_IO_BUFFER_LENGTH
) {
    val buffer = ByteArray(bufferLength)
    var readLength = 0
    while (input.read(buffer).apply { readLength = this } > 0) {
        output.write(buffer, 0, readLength)
    }
}

fun JSONArray.toListString(): List<String> {
    return (0 until this.length()).map { this.getString(it) }
}

fun JSONObject.toMapString(): Map<String, String> {
    val res = mutableMapOf<String, String>()
    this.keys().forEach { res.put(it, this.opt(it).toString()) }
    return res
}

fun <T> Observable<T>.ignoreError(printLog: Boolean = false): Observable<T> =
        this.onErrorResumeNext { error: Throwable ->
            if (printLog) Timber.d(error)
            Observable.empty<T>()
        }

fun Int.formatSpeed(): String =
        when {
            this < KB -> "${this}B/s"
            this < MB -> "${this / KB}KB/s"
            else -> "${this / MB}MB/s"
        }

fun String.toJsonOrNull(): JSONObject? {
    catchAll { return JSONObject(this) }
    return null
}

fun <T> List<T>.toImmutable(): List<T> {
    // clone original then wrap inside unmodifiable list
    return Collections.unmodifiableList(ArrayList(this))
}

fun openUrl(context: Context, url: String, message: String = "Open $url") {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(Intent.createChooser(intent, message))
}

fun openPlayStore(context: Context, packageName: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")))
    } catch (activityNotFound: Throwable) {
        openUrl(context, "https://play.google.com/store/apps/details?id=$packageName",
                context.getString(R.string.open_play_store))
    }
}

fun sendEmail(context: Context, subject: String = "", msg: String = "") {
    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", "help.mediaconverter@gmail.com", null))
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailIntent.putExtra(Intent.EXTRA_TEXT, msg)
    context.startActivity(Intent.createChooser(emailIntent, "Send email..."))
}

fun File.ensureDirExists(): File {
    if (!exists() && !mkdirs()) {
        throw FileNotFoundException("Can't mkdirs $this")
    }
    return this
}

fun File.deleteRecursiveIgnoreError() {
    if (isDirectory) {
        listFilesNotNull().forEach { it.deleteRecursiveIgnoreError() }
    }
    catchAll { this.delete() }
}

fun File.deleteIgnoreError() {
    catchAll { delete() }
}

fun File.listFilesNotNull(): Array<File> {
    val files: Array<File>? = listFiles()
    return files ?: emptyArray()
}

fun String.parseInputUri(): Uri {
    if (startsWith("http", ignoreCase = true)
            || startsWith("content", ignoreCase = true)) {
        return Uri.parse(this)
    } else {
        return Uri.fromFile(File(this))
    }
}

fun String.escapeSingleQuote(): String {
    return replace("'", "'\\''")
}

/**
 * Check if [filename] exists in [folderUri].
 * return Uri of the file existing, otherwise return null
 */
fun Context.checkFileExists(folderUri: Uri, fileName: String): Uri? {
    return if (folderUri.scheme == "file") {
        return File(folderUri.path, fileName).let { if (it.exists()) Uri.fromFile(it) else null }
    } else catchAll {
        val documentTree = DocumentFile.fromTreeUri(this, folderUri)
        documentTree.findFile(fileName).uri
    }
}