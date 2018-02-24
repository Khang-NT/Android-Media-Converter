package com.github.khangnt.mcp.ui.presetcmd

import com.github.khangnt.mcp.ui.BaseFragment

/**
 * Created by Khang NT on 2/9/18.
 * Email: khang.neon.1997@gmail.com
 */

abstract class ConvertFragment: BaseFragment() {
    /**
     * If this fragment is attached to [ConvertActivity], every time user want to quit, the activity
     * will call this method to check if it's safe to quit immediately or not.
     * Activity should ask user to confirm when it's not safe to quit.
     *
     * Return false if fragment state is valuable, and user need to confirm before quit.
     * Otherwise return true. Default implement always return true.
     */
    open fun shouldQuit(): Boolean {
        return true
    }
}