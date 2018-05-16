package com.github.khangnt.mcp.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.util.onTextChanged
import kotlin.math.max

class InputDialogFragment : DialogFragment() {

    interface CheckInputCallback {
        fun getInputError(dialog: InputDialogFragment, input: String): String?
    }

    interface Callbacks {
        fun onInputEntered(dialog: InputDialogFragment, finalInput: String)
        fun onInputCancelled(dialog: InputDialogFragment)
    }

    var checkInputCallback: CheckInputCallback? = null

    // configuration
    private val enableError by lazy {
        arguments?.getBoolean(ARG_ENABLE_ERROR, false) ?: false
    }
    private val hint by lazy { arguments?.getString(ARG_HINT) ?: "Input" }
    private val title by lazy { arguments?.getString(ARG_TITLE) }
    private val positiveButText by lazy {
        arguments?.getString(ARG_POSITIVE_BUTTON) ?: getString(R.string.action_ok)
    }
    private val negativeButText by lazy { arguments?.getString(ARG_NEGATIVE_BUTTON) }
    private val cancelable by lazy {
        arguments?.getBoolean(ARG_CANCELABLE, true) ?: true
    }
    private val maxLines by lazy {
        arguments?.getInt(ARG_MAX_LINES, 1) ?: 1
    }

    private val textInputLayout by lazy { dialog.findViewById<TextInputLayout>(R.id.textInputLayout) }
    private val textInputEditText by lazy { dialog.findViewById<TextInputEditText>(R.id.editText) }

    private lateinit var inputText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inputText = savedInstanceState?.getString(INPUT_TEXT, "")
                ?: arguments?.getString(ARG_INIT_VALUE) ?: ""
        checkInputCallback = (activity as? CheckInputCallback)
                ?: (parentFragment as? CheckInputCallback)
    }

    private fun getCallbacks(): Callbacks {
        return (activity as? Callbacks) ?: (parentFragment as? Callbacks)
            ?: throw IllegalStateException(
                "Parent activity/fragment must implement InputDialogFragment.Callbacks")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            if (title != null) setTitle(title)
            setCancelable(cancelable)
            setPositiveButton(positiveButText) { _, _ ->
                getCallbacks().onInputEntered(this@InputDialogFragment,
                        textInputEditText.text.toString())
            }
            if (negativeButText != null || cancelable) {
                setNegativeButton(negativeButText ?: getString(R.string.action_cancel)) { dialog, _ ->
                    dialog.cancel()
                }
            }
            setView(R.layout.dialog_input)
        }.show()
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        textInputLayout.isErrorEnabled = enableError
        textInputLayout.hint = hint
        if (maxLines != 1) {
            textInputEditText.setSingleLine(false)
            textInputEditText.maxLines = maxLines
            textInputEditText.minLines = Math.max(3, maxLines)
        }
        textInputEditText.setText(inputText)
        dialog as AlertDialog
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        textInputEditText.onTextChanged { text ->
            inputText = text.toString()
            if (enableError) {
                val error = checkInputCallback?.getInputError(this, inputText)
                textInputLayout.error = error
                positiveButton.isEnabled = error == null
            }
        }
        positiveButton.isEnabled = checkInputCallback?.getInputError(this, inputText) == null
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        getCallbacks().onInputCancelled(this@InputDialogFragment)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INPUT_TEXT, inputText)
    }

    class Builder {
        private val bundle = Bundle()

        fun setEnableError(enable: Boolean) = apply { bundle.putBoolean(ARG_ENABLE_ERROR, enable) }

        fun setHint(hint: String) = apply { bundle.putString(ARG_HINT, hint) }

        fun setTitle(title: String) = apply { bundle.putString(ARG_TITLE, title) }

        fun setInitValue(value: String) = apply { bundle.putString(ARG_INIT_VALUE, value) }

        fun setPositiveBut(text: String) = apply { bundle.putString(ARG_POSITIVE_BUTTON, text) }

        fun setNegativeBut(text: String) = apply { bundle.putString(ARG_NEGATIVE_BUTTON, text) }

        fun setCancelable(cancelable: Boolean) = apply { bundle.putBoolean(ARG_CANCELABLE, cancelable) }

        fun setMaxLines(maxLines: Int) = apply { bundle.putInt(ARG_MAX_LINES, maxLines) }

        fun setExtra(key: String, value: Any) = apply {
            when(value) {
                is Int -> bundle.putInt(key, value)
                is String -> bundle.putString(key, value)
                else -> RuntimeException("Add type ${value.javaClass} here")
            }
        }

        fun build() = InputDialogFragment().apply {
            arguments = Bundle().apply { putAll(bundle) }
        }
    }

    companion object {
        private const val INPUT_TEXT = "inputText"

        private const val ARG_ENABLE_ERROR = "arg:enable_error"
        private const val ARG_HINT = "arg:hint"
        private const val ARG_TITLE = "arg:title"
        private const val ARG_INIT_VALUE = "arg:init_value"
        private const val ARG_POSITIVE_BUTTON = "arg:positive_button"
        private const val ARG_NEGATIVE_BUTTON = "arg:positive_button"
        private const val ARG_CANCELABLE = "arg:cancelable"
        private const val ARG_MAX_LINES = "arg:max_lines"
    }
}