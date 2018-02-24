package com.github.khangnt.mcp.ui.presetcmd

import com.github.khangnt.mcp.ui.BaseFragment

/**
 * Created by Khang NT on 2/9/18.
 * Email: khang.neon.1997@gmail.com
 */

abstract class ConvertFragment: BaseFragment() {

    interface OnSubmittedListener {
        fun onSubmitted(fragment: ConvertFragment)
    }

    /**
     * If this fragment is attached to [ConvertActivity], every time user want to quit, the activity
     * will call this method to check whether this ConvertFragment has modified data or not.
     *
     * Return true if Activity should ask user to confirm to discard changes.
     * Otherwise return false. (default return false)
     */
    open fun shouldConfirmDiscardChanges(): Boolean {
        return false
    }
}