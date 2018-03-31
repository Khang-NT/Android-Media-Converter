package com.github.khangnt.mcp.ui.presetcmd.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.util.openKeyboard
import kotlinx.android.synthetic.main.fragment_trimmer.*

/**
 * Created by Simon Pham on 3/30/18.
 * Email: simonpham.dn@gmail.com
 */

data class BeginEndPosition(val isTrimmed: Boolean, val beginPos: Float, val endPos: Float, val isError: Boolean)

class TrimmerFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_trimmer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    @SuppressLint("SetTextI18n")
    fun validateAndGetBeginEndPostition(callback: (BeginEndPosition) -> Unit) {
        // validate
        if ((edStartPos.text.toString() == "" && edEndPos.text.toString() == "")) {
            callback(BeginEndPosition(false, 0.0f, 0.0f, false))
            return
        }

        if (edStartPos.text.toString() == "" && edEndPos.text.toString() != "") {
            edStartPos.setText("0")
        }

        if (edEndPos.text.toString() == "" && edStartPos.text.toString() != "") {
            edEndPos.setText("9999999")
        }

        if (edStartPos.text.toString().startsWith(".")) {
            edStartPos.setText("0" + edStartPos.text)
        }
        if (edStartPos.text.toString().endsWith(".")) {
            edStartPos.setText("${edStartPos.text}0")
        }

        if (edEndPos.text.toString().startsWith(".")) {
            edEndPos.setText("0" + edEndPos.text)
        }
        if (edEndPos.text.toString().endsWith(".")) {
            edEndPos.setText("${edEndPos.text}0")
        }

        val startPoint = edStartPos.text.toString().toFloat()
        val endPoint = edEndPos.text.toString().toFloat() - startPoint

        if (endPoint <= 0.0f) {
            edEndPos.error = getString(R.string.error_invalid_position)
            edEndPos.openKeyboard()
            callback(BeginEndPosition(false, startPoint, endPoint, true))
            return
        }

        callback(BeginEndPosition(true, startPoint, endPoint, false))
    }

    fun getMarkBeginButton(): Button {
        return btnMarkBegin
    }

    fun getMarkEndButton(): Button {
        return btnMarkEnd
    }

    fun getStartPos(): EditText {
        return edStartPos
    }

    fun getEndPos(): EditText {
        return edEndPos
    }

}