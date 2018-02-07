package com.github.khangnt.mcp.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.widget.Toast
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.hasWriteStoragePermission
import com.github.khangnt.mcp.util.toast

/**
 * Created by Khang NT on 1/7/18.
 * Email: khang.neon.1997@gmail.com
 */

const val EXTRA_PENDING_INTENT = "PermissionTransparentActivity.PendingIntent"
const val EXTRA_DENIED_MESSAGE = "PermissionTransparentActivity.DeniedMessage"

private const val RC = 1234

class PermissionTransparentActivity : BaseActivity() {

    private var pendingIntent: PendingIntent? = null
    private var permissionDeniedMess: String? = null

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT)
        permissionDeniedMess = intent.getStringExtra(EXTRA_DENIED_MESSAGE)

        if (pendingIntent === null) {
            finish()
            return
        }

        if (hasWriteStoragePermission(this)) {
            catchAll { pendingIntent?.send() }
            return
        }

        requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE), RC)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.any { it != PERMISSION_GRANTED }) {
            toast(permissionDeniedMess ?: getString(R.string.permission_not_granted), Toast.LENGTH_LONG)
        } else {
            catchAll { pendingIntent?.send() }
        }

        finish()
    }
}