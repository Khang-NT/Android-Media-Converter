package com.github.khangnt.mcp.ui.prefs

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.khangnt.mcp.R

/**
 * Created by Simon Pham on 2019-06-29.
 * Email: simonpham.dn@gmail.com
 */

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}