package com.github.khangnt.mcp.ui

import android.os.Bundle
import android.support.v4.app.Fragment


class MainActivity : SingleFragmentActivity() {
    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment = MainFragment()
}
