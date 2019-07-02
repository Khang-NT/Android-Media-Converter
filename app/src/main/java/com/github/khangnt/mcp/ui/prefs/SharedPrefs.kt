package com.github.khangnt.mcp.ui.prefs

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import com.github.khangnt.mcp.R

class SharedPrefs(private val mContext: Context) {
    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)

    var isRated: Boolean
        get() = getBoolean(R.string.pref_key_is_rated, false)
        set(rated) = putBoolean(R.string.pref_key_is_rated, rated)

    var successJobsCount: Int
        get() = getInt(R.string.pref_key_success_jobs_count, 0)
        set(jobSuccessCount) = putInt(R.string.pref_key_success_jobs_count, jobSuccessCount)

    var delayRateDialogUntil: Long
        get() = getLong(R.string.pref_key_rate_dialog_delay, 0)
        set(delayUntil) = putLong(R.string.pref_key_rate_dialog_delay, delayUntil)

    var lastOutputFolderUri: String?
        get() = getString(R.string.pref_key_last_output_folder, null)
        set(value) = putString(R.string.pref_key_last_output_folder, value)

    var lastKnownVersionCode: Int
        get() = getInt(R.string.pref_key_last_version_code, 0)
        set(lastVersionCode) = putInt(R.string.pref_key_last_version_code, lastVersionCode)

    var rememberCommandConfig: Boolean
        get() = getBoolean(R.string.pref_key_remember_commands, true)
        set(rememberCommandConfig) = putBoolean(R.string.pref_key_remember_commands, rememberCommandConfig)

    var lastMp3Configs: String?
        get() = getString(R.string.pref_key_last_mp3_configs, "{}")
        set(commandConfigs) = putString(R.string.pref_key_last_mp3_configs, commandConfigs)

    var lastAacConfigs: String?
        get() = getString(R.string.pref_key_last_aac_configs, "{}")
        set(commandConfigs) = putString(R.string.pref_key_last_aac_configs, commandConfigs)

    private operator fun contains(@StringRes keyRes: Int): Boolean {
        return mPrefs.contains(mContext.getString(keyRes))
    }

    private fun getBoolean(@StringRes keyRes: Int, defaultValue: Boolean): Boolean {
        return mPrefs.getBoolean(mContext.getString(keyRes), defaultValue)
    }

    private fun getInt(@StringRes keyRes: Int, defaultValue: Int): Int {
        return mPrefs.getInt(mContext.getString(keyRes), defaultValue)
    }

    private fun getLong(@StringRes keyRes: Int, defaultValue: Long): Long {
        return mPrefs.getLong(mContext.getString(keyRes), defaultValue)
    }

    private fun getString(@StringRes keyRes: Int, defaultValue: String?): String? {
        return mPrefs.getString(mContext.getString(keyRes), defaultValue)
    }

    private fun getStringSet(@StringRes keyRes: Int): Set<String> {
        return mPrefs.getStringSet(mContext.getString(keyRes), emptySet()) as Set<String>
    }

    private fun putBoolean(@StringRes keyRes: Int, value: Boolean) {
        mPrefs.edit()
                .putBoolean(mContext.getString(keyRes), value)
                .apply()
    }

    private fun putInt(@StringRes keyRes: Int, value: Int) {
        mPrefs.edit()
                .putInt(mContext.getString(keyRes), value)
                .apply()
    }

    private fun putLong(@StringRes keyRes: Int, value: Long) {
        mPrefs.edit()
                .putLong(mContext.getString(keyRes), value)
                .apply()
    }

    private fun putString(@StringRes keyRes: Int, value: String?) {
        mPrefs.edit()
                .putString(mContext.getString(keyRes), value)
                .apply()
    }

    private fun putStringSet(@StringRes keyRes: Int, value: Set<String>) {
        mPrefs.edit()
                .putStringSet(mContext.getString(keyRes), value)
                .apply()
    }

}