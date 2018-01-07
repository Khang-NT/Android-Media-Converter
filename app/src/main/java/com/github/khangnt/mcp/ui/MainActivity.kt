package com.github.khangnt.mcp.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.khangnt.mcp.util.hasWriteStoragePermission


class MainActivity : SingleFragmentActivity() {
    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment = MainFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasWriteStoragePermission(this)) {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 0)
        }
    }
}
