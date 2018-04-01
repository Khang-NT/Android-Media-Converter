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

data class StartPositionDuration(val startPos: Float, val duration: Float, val isError: Boolean, val isTrimmed: Boolean)

class TrimmerFragment : BaseFragment() {

    companion object {
        private const val MAX_DURATION = "99999999"
    }

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
        
        btnMarkBegin.setOnClickListener {
            edStartPos.setText("0")
        }

        btnMarkEnd.setOnClickListener {
            edEndPos.setText(MAX_DURATION)
        }
    }

    @SuppressLint("SetTextI18n")
    fun validateAndGetStartPositionDuration(callback: (StartPositionDuration) -> Unit) {
        // validate
        if ((edStartPos.text.toString() == "" && edEndPos.text.toString() == "")) {
            callback(StartPositionDuration(0.0f, 0.0f, false, false))
            return
        }

        if (edStartPos.text.toString() == "" && edEndPos.text.toString() != "") {
            edStartPos.setText("0")
        }

        if (edEndPos.text.toString() == "" && edStartPos.text.toString() != "") {
            edEndPos.setText(MAX_DURATION)
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
        val duration = edEndPos.text.toString().toFloat() - startPoint

        if (duration <= 0.0f) {
            edEndPos.error = getString(R.string.error_invalid_position)
            edEndPos.openKeyboard()
            callback(StartPositionDuration(startPoint, duration, true, false))
            return
        }

        callback(StartPositionDuration(startPoint, duration,false, true))
    }

}