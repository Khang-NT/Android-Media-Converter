package com.github.khangnt.mcp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances.Companion.getSharedPrefs
import com.github.khangnt.mcp.ui.SHOW_RATE_DIALOG_ACTION
import com.github.khangnt.mcp.util.openUrl

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
        val PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=com.github.khangnt.mcp"
        val rateDialog = AlertDialog.Builder(context!!).create()

        rateDialog.setTitle(context.getString(R.string.rate_us_title))
        rateDialog.setMessage(context.getString(R.string.rate_us_message))

        // Love it button
        rateDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.rate_us_love_it), {
            dialogInterface, i ->
            openUrl(context, PLAYSTORE_URL)
            getSharedPrefs().isRated = true
        })

        // Not now button
        rateDialog.setButton(AlertDialog.BUTTON_NEUTRAL, context.getString(R.string.rate_us_not_now), {
            dialogInterface, i ->
            // do nothing
        })

        // Never button
        rateDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.rate_us_never), {
            dialogInterface, i ->
            getSharedPrefs().isRated = true
        })

        rateDialog.show()
    }

}