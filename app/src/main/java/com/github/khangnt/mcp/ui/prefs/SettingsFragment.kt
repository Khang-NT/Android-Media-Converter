package com.github.khangnt.mcp.ui.prefs

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.util.toast

/**
 * Created by Simon Pham on 2019-06-29.
 * Email: simonpham.dn@gmail.com
 */

class SettingsFragment : PreferenceFragmentCompat() {

    private val sharedPrefs = SingletonInstances.getSharedPrefs()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            getString(R.string.pref_key_restore_default_commands) -> {
                onRestoreDefaultCommandsClick()
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private fun onRestoreDefaultCommandsClick() {
        AlertDialog.Builder(context!!)
                .setMessage(getString(R.string.message_restore_default_commands))
                .setPositiveButton(R.string.action_ok) { _, _ ->
                    val empty = "{}"
                    sharedPrefs.apply {
                        lastAacConfigs = empty
                        lastFlacConfigs = empty
                        lastMp3Configs = empty
                        lastMp4Configs = empty
                        lastOggConfigs = empty
                        lastOpusConfigs = empty
                    }
                    toast(getString(R.string.message_restored_default_commands_success))
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }
}