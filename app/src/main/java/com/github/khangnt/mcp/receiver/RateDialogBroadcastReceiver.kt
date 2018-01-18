package com.github.khangnt.mcp.receiver

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.SHOW_RATE_DIALOG_ACTION

/**
 * Created by Simon Pham on 1/18/2018.
 * Email: simonpham.dn@gmail.com
 */

class RateDialogBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        when (p1?.action) {
            SHOW_RATE_DIALOG_ACTION -> showRateDialog(p0)
        }
    }

    private fun showRateDialog(context: Context?) {
        val alertDilog = AlertDialog.Builder(context).create()
        alertDilog.setTitle(context.getString(R.string.rate_us_title))
        alertDilog.setMessage(context.getString(R.string.rate_us_message))

        alertDilog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.rate_us_love_it), {
            dialogInterface, i ->
            Toast.makeText(context, "Love it!", Toast.LENGTH_SHORT).show()      // for testing
        })

        alertDilog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.rate_us_not_now), {
            dialogInterface, j ->
            Toast.makeText(context, "Not now", Toast.LENGTH_SHORT).show()       // for testing
        })
        alertDilog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.rate_us_never), {
            dialogInterface, k ->
            Toast.makeText(context, "Never", Toast.LENGTH_SHORT).show()         // for testing
        })

        alertDilog.show()
    }

}