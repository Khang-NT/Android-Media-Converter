package com.github.khangnt.mcp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

        btnFRequest.isSelected = true
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

        btnFRequest.setOnClickListener{
            btnFRequest.isSelected = true
            btnBReport.isSelected = false
            btnQuestion.isSelected = false
        }

        btnBReport.setOnClickListener{
            btnFRequest.isSelected = false
            btnBReport.isSelected = true
            btnQuestion.isSelected = false
        }

        btnQuestion.setOnClickListener{
            btnFRequest.isSelected = false
            btnBReport.isSelected = false
            btnQuestion.isSelected = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}