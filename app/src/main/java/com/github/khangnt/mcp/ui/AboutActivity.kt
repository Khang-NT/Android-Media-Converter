package com.github.khangnt.mcp.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.ScrollView
import com.github.khangnt.mcp.*
import com.github.khangnt.mcp.util.openPlayStore
import com.github.khangnt.mcp.util.openUrl
import com.github.khangnt.mcp.util.sendEmail
import com.github.khangnt.mcp.util.toast
import de.psdev.licensesdialog.LicensesDialog
import kotlinx.android.synthetic.main.activity_about.*


/**
 * About page UI is inspired by Phonograph
 * https://github.com/kabouzeid/Phonograph
 */

class AboutActivity : BaseActivity() {

    private var cardHeight: Float = 0.0f

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val device = DeviceInfo(this)

        cardHeight = cardFeedback.height.toFloat()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val scrollRange by lazy {
            // decrease scrim visible height (-1/4)
            collapsingToolbar.scrimVisibleHeightTrigger =
                    collapsingToolbar.scrimVisibleHeightTrigger * 3 / 4
            appBarLayout.totalScrollRange
        }

        appBarLayout.addOnOffsetChangedListener { _, offset ->
            if (scrollRange + offset == 0) {
                toolbar.navigationIcon?.let { DrawableCompat.setTint(it, Color.WHITE) }
            } else if (offset == 0) {
                toolbar.navigationIcon?.let { DrawableCompat.setTint(it, Color.BLACK) }
            }
        }

        tvAppVersion.text = getString(R.string.app_version_format, BuildConfig.VERSION_NAME)

        rateUs.setOnClickListener {
            openPlayStore(this, PLAY_STORE_PACKAGE)
        }

        forkOnGithub.setOnClickListener {
            openUrl(this, GITHUB_REPO, getString(R.string.open_github))
        }

        licenses.setOnClickListener {
            LicensesDialog.Builder(this)
                    .setNotices(R.raw.licenses)
                    .build()
                    .show()
        }

        contact.setOnClickListener {
            sendEmail(this)
        }

        translate.setOnClickListener {
            openUrl(this, TRANSLATE_PAGE, getString(R.string.open_translate_page))
        }

        bugReport.setOnClickListener {
            // copy device info to clipboard
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Device Info", device.toString())
            clipboard.primaryClip = clip
            toast(getString(R.string.prompt_device_info_copied))

            openUrl(this, GITHUB_ISSUE, getString(R.string.open_github_issues))
        }

        emailFeedback.setOnClickListener {
            toggleFeedbackCard()
        }

        btnSendFB.setOnClickListener {
            toggleFeedbackCard()

            var feedBackType = ""

            when (tabLayout.selectedTabPosition) {
                0 -> feedBackType = getString(R.string.feature_request)
                1 -> feedBackType = getString(R.string.bug_report)
                2 -> feedBackType = getString(R.string.question)
            }

            sendEmail(
                    this,
                    "$feedBackType from ${device.getModel()} Android ${device.getAndroidVersion()}",
                    "${edFeedbackDetails.text}\n\n\n\n$device"
            )
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun toggleFeedbackCard() {
        if (cardFeedback.visibility == View.GONE) {
            cardFeedback.visibility = View.VISIBLE
            cardFeedback.alpha = 0.0f

            cardFeedback.animate()
                    .translationY(cardHeight)
                    .alpha(1.0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            edFeedbackDetails.requestFocus()
                        }

                        override fun onAnimationStart(animation: Animator?) {
                            super.onAnimationStart(animation)
                            scrollView.post({ scrollView.fullScroll(ScrollView.FOCUS_DOWN) })
                        }
                    })
        } else {
            cardFeedback.animate()
                    .translationY(0.0f)
                    .alpha(0.0f)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            cardFeedback.visibility = View.GONE
                        }
                    })
        }
    }

}