package com.github.khangnt.mcp.ui.jobmaker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.STEP_FIVE_AD_UNIT_ID
import com.github.khangnt.mcp.ui.MainActivity.Companion.openJobManagerIntent
import com.github.khangnt.mcp.util.getViewModel
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.fragment_create_job_success.*

/**
 * Created by Khang NT on 4/11/18.
 * Email: khang.neon.1997@gmail.com
 */

private var currentNativeAd: UnifiedNativeAd? = null

class AdvertiseFragment : StepFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }

    override fun onDestroy() {
        currentNativeAd?.destroy()
        super.onDestroy()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_create_job_success, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeAds(context, unifiedNativeAdView)

        btnContinue.setOnClickListener {
            onGoToNextStep()
        }

        btnExit.setOnClickListener {
            activity!!.finish()
            openJobManagerIntent(it.context)
        }
    }

    override fun onGoToNextStep() {
        // jobMakerViewModel.getCommandConfig().makeJobs(final outputs)
        jobMakerViewModel.postReset()
    }

    private fun initializeAds(context: Context?, unifiedNativeAdView: View) {
        val builder = AdLoader.Builder(context, STEP_FIVE_AD_UNIT_ID)

        builder.forUnifiedNativeAd { nativeAd ->
            val adView = unifiedNativeAdView as UnifiedNativeAdView
            populateUnifiedNativeAdView(nativeAd, adView)
        }

        val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()

        val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()

        builder.withNativeAdOptions(adOptions)

        builder.build().loadAd(AdRequest.Builder().build())
    }


    private fun populateUnifiedNativeAdView(newAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        currentNativeAd?.destroy()
        currentNativeAd = newAd
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = newAd.headline
        adView.mediaView.setMediaContent(newAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (newAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = newAd.body
        }

        if (newAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = newAd.callToAction
        }

        if (newAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                    newAd.icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (newAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = newAd.price
        }

        if (newAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = newAd.store
        }

        if (newAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = newAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (newAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = newAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        adView.visibility = View.VISIBLE
        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(newAd)
    }
}