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

fun hasWriteStoragePermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                (context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED &&
                        context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PERMISSION_GRANTED)