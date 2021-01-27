package com.github.khangnt.mcp.ui.prefs

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.ui.SingleFragmentActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * Created by Simon Pham on 2019-06-29.
 * Email: simonpham.dn@gmail.com
 */

class SettingsActivity : SingleFragmentActivity() {

    private val mRewardedVideoAd: RewardedVideoAd by lazy { MobileAds.getRewardedVideoAdInstance(this) }
    private val sharedPrefs = SingletonInstances.getSharedPrefs()

    companion object {

        fun launch(context: Context) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }
    }

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment {
        return SettingsFragment(mRewardedVideoAd)
    }

    override fun onCreateLayout(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val scrollRange by lazy {
            // decrease scrim visible height (-1/4)
            collapsingToolbar.scrimVisibleHeightTrigger =
                    collapsingToolbar.scrimVisibleHeightTrigger * 3 / 4
            appBarLayout.totalScrollRange
        }

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
            if (scrollRange + offset == 0) {
                toolbar.navigationIcon?.let { DrawableCompat.setTint(it, Color.WHITE) }
            } else if (offset == 0) {
                toolbar.navigationIcon?.let { DrawableCompat.setTint(it, Color.BLACK) }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onResume() {
        mRewardedVideoAd.resume(this)
        super.onResume()
    }

    override fun onPause() {
        mRewardedVideoAd.pause(this)
        super.onPause()
    }

    override fun onDestroy() {
        mRewardedVideoAd.destroy(this)
        super.onDestroy()
    }
}