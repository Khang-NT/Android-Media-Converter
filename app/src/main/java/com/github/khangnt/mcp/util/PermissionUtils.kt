package com.github.khangnt.mcp.util

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build

/**
 * Created by Khang NT on 1/7/18.
 * Email: khang.neon.1997@gmail.com
 */

val appPermissions = arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)

fun hasWriteStoragePermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                appPermissions.none { context.checkSelfPermission(it) != PERMISSION_GRANTED }