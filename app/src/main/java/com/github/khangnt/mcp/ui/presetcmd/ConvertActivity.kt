package com.github.khangnt.mcp.ui.presetcmd

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.SingleFragmentActivity
import com.github.khangnt.mcp.ui.presetcmd.mp3.ConvertMp3Fragment
import kotlinx.android.synthetic.main.activity_convert.*

/**
 * Created by Khang NT on 2/3/18.
 * Email: khang.neon.1997@gmail.com
 */

private const val EXTRA_TITLE = "ConvertActivity:title"
private const val EXTRA_CONVERT_ID = "ConvertActivity:convert_id"

class ConvertActivity: SingleFragmentActivity() {

    companion object {
        fun launch(context: Context, title: String, convertId: Int) {
            context.startActivity(Intent(context, ConvertActivity::class.java)
                    .putExtra(EXTRA_TITLE, title)
                    .putExtra(EXTRA_CONVERT_ID, convertId))
        }
    }

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment {
        return ConvertMp3Fragment()
    }

    override fun onCreateLayout(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_convert)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = intent.getStringExtra(EXTRA_TITLE)
        collapsingToolbar.title = title
    }

    override fun getFragmentContainerId(): Int {
        return R.id.convertFragmentContainer
    }

    override fun onSupportNavigateUp(): Boolean {
        promptExitDialog()
        return true
    }

    override fun onBackPressed() {
        promptExitDialog()
    }

    private fun promptExitDialog() {
        AlertDialog.Builder(this)
                .setTitle("Do you want to exit?")
                .setMessage("Your settings won't be saved")
                .setPositiveButton(R.string.action_yes, {_, _ -> finish()})
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

}