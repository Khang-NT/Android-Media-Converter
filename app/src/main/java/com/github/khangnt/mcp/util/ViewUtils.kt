package com.github.khangnt.mcp.util

import android.content.Context
import android.os.Build
import android.support.v4.view.ViewCompat


/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

fun isRtl(context: Context): Boolean {
    return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN &&
            context.resources.configuration.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
}