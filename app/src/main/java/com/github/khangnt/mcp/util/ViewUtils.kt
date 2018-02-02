package com.github.khangnt.mcp.util

import android.content.Context
import android.os.Build
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.*
import android.widget.TextView
import android.widget.Toast


/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

fun isRtl(context: Context): Boolean {
    return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN &&
            context.resources.configuration.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
}

fun TextView.onTextSizeChanged(listener: (length: Int) -> Unit): TextWatcher {
    val textWatcher: TextWatcher = object : TextWatcher {
        var length = this@onTextSizeChanged.length()

        override fun afterTextChanged(s: Editable) {
            if (s.length != length) {
                length = s.length
                listener(length)
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }
    addTextChangedListener(textWatcher)
    return textWatcher
}

private fun showToast(context: Context, message: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message ?: "null", duration).show()
}

fun Context.toast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    toast(getString(messageRes), duration)
}

fun Context.toast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    showToast(this, message, duration)
}

fun Fragment.toast(@StringRes messageRes: Int, duration: Int = Toast.LENGTH_SHORT) {
    toast(getString(messageRes), duration)
}

fun Fragment.toast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    showToast(context!!, message, duration)
}


fun View.visible() {
    visibility = VISIBLE
}

fun View.invisible() {
    visibility = INVISIBLE
}

fun View.gone() {
    visibility = GONE
}