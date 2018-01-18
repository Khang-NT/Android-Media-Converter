package com.github.khangnt.mcp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.github.khangnt.mcp.SHOW_RATE_DIALOG_ACTION

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
        Toast.makeText(context, "Broadcast received!", 3000).show()     // for testing
    }

    // nothing

}