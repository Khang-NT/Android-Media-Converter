package com.github.khangnt.mcp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.github.khangnt.mcp.util.*
import de.psdev.licensesdialog.LicensesDialog
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }

        private const val KEY_CURRENT_TAB = "AboutActivity.currentTab"
        private const val KEY_CARD_FEEDBACK_VISIBILITY = "AboutActivity.cardFbVisibility"
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
            viewChangelog(this, resources)
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
            val clip = ClipData.newPlainText("Device Info", device.toMarkdown())
            clipboard.primaryClip = clip
            toast(getString(R.string.prompt_device_info_copied))

            openUrl(this, GITHUB_NEW_ISSUE_URL, getString(R.string.open_github_issues))
        }

        emailFeedback.setOnClickListener {
            toggleFeedbackCard()
        }

        btnSendFb.setOnClickListener {
            toggleFeedbackCard()

            val feedBackType = when (tabLayout.selectedTabPosition) {
                0 -> getString(R.string.feature_request)
                1 -> getString(R.string.bug_report)
                2 -> getString(R.string.question)
                else -> null
            }

            sendEmail(
                    this,
                    "$feedBackType from ${device.getModel()} Android ${device.getAndroidVersion()}",
                    "${edFeedbackDetails.text}\n\n\n\n$device"
            )
        }

        edFeedbackDetails.setOnFocusChangeListener { _, focused ->
            if (focused) {
                scrollView.post({ scrollView.fullScroll(ScrollView.FOCUS_DOWN) })
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun toggleFeedbackCard() {
        val transition = TransitionSet()
        transition.addTransition(Fade())
        transition.addTransition(Slide(Gravity.START))
        TransitionManager.beginDelayedTransition(transitionsContainer, transition)

        if (cardFeedback.visibility == View.GONE) {
            cardFeedback.visibility = View.VISIBLE
            scrollView.post({ scrollView.fullScroll(ScrollView.FOCUS_DOWN) })
        } else {
            cardFeedback.visibility = View.GONE
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt(KEY_CURRENT_TAB, tabLayout.selectedTabPosition)
        savedInstanceState.putInt(KEY_CARD_FEEDBACK_VISIBILITY, cardFeedback.visibility)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val currentTab = savedInstanceState.getInt(KEY_CURRENT_TAB)
        tabLayout.getTabAt(currentTab)?.select()
        cardFeedback.visibility = savedInstanceState.getInt(KEY_CARD_FEEDBACK_VISIBILITY)
    }

}