package com.github.khangnt.mcp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.BuildConfig
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.util.openUrl
import de.psdev.licensesdialog.LicensesDialog
import kotlinx.android.synthetic.main.fragment_about.*



/**
 * About page UI is inspired by Phonograph
 * https://github.com/kabouzeid/Phonograph
 */

class AboutFragment: BaseFragment() {
    private val GITHUB_REPO = "https://github.com/Khang-NT/Android-Media-Converter"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_about, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvAppVersion.text = getString(R.string.app_version_format, BuildConfig.VERSION_NAME)

        forkOnGithub.setOnClickListener {
            openUrl(view.context, GITHUB_REPO)
        }

        licenses.setOnClickListener {
            LicensesDialog.Builder(view.context)
                    .setNotices(R.raw.licenses)
                    .build()
                    .show()
        }
    }

}