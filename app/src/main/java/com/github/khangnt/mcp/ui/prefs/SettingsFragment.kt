package com.github.khangnt.mcp.ui.prefs

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.RecyclerView
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.util.toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener

/**
 * Created by Simon Pham on 2019-06-29.
 * Email: simonpham.dn@gmail.com
 */

class SettingsFragment(private val mRewardedVideoAd: RewardedVideoAd) : PreferenceFragmentCompat() {

    private val sharedPrefs = SingletonInstances.getSharedPrefs()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        mRewardedVideoAd.loadAd(getString(R.string.reward_ad_unit_id), AdRequest.Builder().build())
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen?): RecyclerView.Adapter<*> {
        val switchAds = preferenceScreen?.findPreference<SwitchPreference>(getString(R.string.pref_key_enable_ads))
        if (switchAds != null && !switchAds.isChecked) {
            switchAds.isEnabled = true
        }

        mRewardedVideoAd.rewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onRewardedVideoAdClosed() {}

            override fun onRewardedVideoAdLeftApplication() {}

            override fun onRewardedVideoAdLoaded() {}

            override fun onRewardedVideoAdOpened() {}

            override fun onRewardedVideoCompleted() {}

            override fun onRewarded(p0: RewardItem?) {
                sharedPrefs.enabledAds = false
                switchAds?.isChecked = false
                switchAds?.isEnabled = true
            }

            override fun onRewardedVideoStarted() {}

            override fun onRewardedVideoAdFailedToLoad(p0: Int) {}
        }
        return super.onCreateAdapter(preferenceScreen)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            getString(R.string.pref_key_restore_default_commands) -> {
                onRestoreDefaultCommandsClick()
                true
            }
            getString(R.string.pref_key_watch_an_ad) -> {
                if (mRewardedVideoAd.isLoaded) {
                    mRewardedVideoAd.show()
                } else {
                    toast(R.string.title_loading_ads_try_again)
                }
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