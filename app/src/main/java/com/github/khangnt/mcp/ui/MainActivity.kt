package com.github.khangnt.mcp.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.khangnt.mcp.BuildConfig
import com.github.khangnt.mcp.receiver.RateDialogBroadcastReceiver
import com.github.khangnt.mcp.util.hasWriteStoragePermission

const val SHOW_RATE_DIALOG_ACTION = BuildConfig.APPLICATION_ID + ".show_rate_dialog"

class MainActivity : SingleFragmentActivity() {

    val rateDialogBroadcastReceiver = RateDialogBroadcastReceiver()
    val intentFilter = IntentFilter(SHOW_RATE_DIALOG_ACTION)

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment = MainFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasWriteStoragePermission(this)) {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 0)
        }
        checkJobs()
    }

    private fun checkJobs() {
        // TBD
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(rateDialogBroadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(rateDialogBroadcastReceiver)
    }

}
