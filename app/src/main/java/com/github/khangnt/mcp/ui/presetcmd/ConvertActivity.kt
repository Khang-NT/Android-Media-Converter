package com.github.khangnt.mcp.ui.presetcmd

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import com.github.khangnt.mcp.PresetCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.SingleFragmentActivity
import kotlinx.android.synthetic.main.activity_convert.*

/**
 * Created by Khang NT on 2/3/18.
 * Email: khang.neon.1997@gmail.com
 */

private const val EXTRA_PRESET_COMMAND_ID = "ConvertActivity:preset_command_id"

class ConvertActivity : SingleFragmentActivity() {

    companion object {
        fun launchIntent(context: Context, presetCommandId: Int): Intent {
            return Intent(context, ConvertActivity::class.java)
                    .putExtra(EXTRA_PRESET_COMMAND_ID, presetCommandId)
        }
    }

    private val presetCommand: PresetCommand by lazy {
        val id = intent.getIntExtra(EXTRA_PRESET_COMMAND_ID, -1)
        PresetCommand.values()[id]
    }

    override fun onCreateFragment(savedInstanceState: Bundle?): Fragment {
        return presetCommand.convertFragmentFactory.invoke()
    }

    override fun onCreateLayout(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_convert)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(presetCommand.titleRes)
        collapsingToolbar.title = title

        collapsingToolbar.contentScrim = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                presetCommand.colors)
        collapsingToolbar.statusBarScrim = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                presetCommand.colors)

        val scrollRange by lazy { appBarLayout.totalScrollRange }
        appBarLayout.addOnOffsetChangedListener { _, offset ->
            if (scrollRange + offset == 0) {
                toolbar.navigationIcon?.let { DrawableCompat.setTint(it, Color.WHITE) }
            } else if (offset == 0) {
                toolbar.navigationIcon?.let { DrawableCompat.setTint(it, Color.BLACK) }
            }
        }
    }

    override fun getFragmentContainerId(): Int {
        return R.id.convertFragmentContainer
    }

    override fun onSupportNavigateUp(): Boolean {
        onQuit()
        return true
    }

    override fun onBackPressed() {
        onQuit()
    }

    private fun onQuit() {
        if ((getContentFragment() as? ConvertFragment)?.shouldQuit() != false) {
            finish()
        } else {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_confirm_quit_title))
                    .setMessage(getString(R.string.dialog_confirm_quit_message))
                    .setPositiveButton(R.string.action_yes, { _, _ -> finish() })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show()
        }
    }

}