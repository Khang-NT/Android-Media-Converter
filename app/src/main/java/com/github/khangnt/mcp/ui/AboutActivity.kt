package com.github.khangnt.mcp.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.ScrollView
import com.github.khangnt.mcp.BuildConfig
import com.github.khangnt.mcp.GITHUB_REPO
import com.github.khangnt.mcp.PLAY_STORE_PACKAGE
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.util.openPlayStore
import com.github.khangnt.mcp.util.openUrl
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
            openUrl(this, GITHUB_REPO, "Open Github")
        }

        licenses.setOnClickListener {
            LicensesDialog.Builder(this)
                    .setNotices(R.raw.licenses)
                    .build()
                    .show()
        }

        val cardHeight = cardFeedback.height.toFloat()
        emailFeedback.setOnClickListener {
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}