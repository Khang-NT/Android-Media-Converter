package com.github.khangnt.mcp.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.support.transition.Fade
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.webkit.WebView
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

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val device = DeviceInfo(this)

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

        changelog.setOnClickListener{
            val web = WebView(this)
            web.loadUrl("file:///android_asset/changelog.html");
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Changelog")
                    .setView(web)
                    .setPositiveButton(getString(R.string.close), null)
                    .show()
        }

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

        edFeedbackDetails.setOnFocusChangeListener { _, _ ->
            scrollView.post({ scrollView.fullScroll(ScrollView.FOCUS_DOWN) })
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun toggleFeedbackCard() {
        val transition = TransitionSet()
        transition.addTransition(Fade())
        transition.addTransition(Slide(Gravity.LEFT))

        TransitionManager.beginDelayedTransition(transitionsContainer, transition)

        if (cardFeedback.visibility == View.GONE) {
            cardFeedback.visibility = View.VISIBLE
            edFeedbackDetails.clearFocus()
            edFeedbackDetails.requestFocus()
        } else {
            cardFeedback.visibility = View.GONE
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {

        savedInstanceState!!.putInt("CurrentTab", tabLayout.selectedTabPosition)
        savedInstanceState!!.putInt("FBCardVisibility", cardFeedback.visibility)

        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {

        super.onRestoreInstanceState(savedInstanceState)

        val currentTab = savedInstanceState.getInt("CurrentTab")
        val tab = tabLayout.getTabAt(currentTab)
        tab!!.select()

        cardFeedback.visibility = savedInstanceState.getInt("FBCardVisibility")

    }

}