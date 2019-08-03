package com.github.khangnt.mcp.util

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.*
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout


/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

fun isRtl(context: Context): Boolean {
    return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN &&
            context.resources.configuration.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
}

fun TextView.onTextSizeChanged(listener: (length: Int) -> Unit): TextWatcher {
    var length = this.length()
    return onTextChanged {
        if (it.length != length) {
            length = it.length
            listener(length)
        }
    }
}

fun TextView.onTextChanged(listener: (text: CharSequence) -> Unit): TextWatcher {
    val textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            listener(text)
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

fun SeekBar.onSeekBarChanged(callback: (progress: Int) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            callback(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
    })
}

fun Spinner.onItemSelected(callback: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            callback(position)
        }
    }
}

var TextInputLayout.errorMessage: String?
    get() = if (isErrorEnabled) error?.toString() else null
    set(value) {
        if (value.isNullOrEmpty()) {
            isErrorEnabled = false
            error = null
        } else {
            isErrorEnabled = true
            error = value
        }
    }

fun EditText.openKeyboard(delay: Long = 200) {
    postDelayed({
        this.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }, delay)
}

fun getSpanCount(minWidth: Int, columnSpace: Int = 0): Int {
    val availableWidth = Resources.getSystem().displayMetrics.widthPixels - columnSpace
    var spanCount = 1
    while (availableWidth - ((minWidth + columnSpace) * (spanCount + 1)) >= 0) {
        spanCount++
    }
    return spanCount
}

fun View.doOnPreDraw(action: () -> Unit) {
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            action()
            viewTreeObserver.removeOnPreDrawListener(this)
            return true
        }
    })
}

fun View.disableInHalfSecond() {
    isEnabled = false
    postDelayed({ isEnabled = true }, 500)
}