package com.github.khangnt.mcp.ui.jobmaker

import com.github.khangnt.mcp.ui.BaseFragment

/**
 * Created by Khang NT on 4/11/18.
 * Email: khang.neon.1997@gmail.com
 */

abstract class StepFragment : BaseFragment() {
    abstract fun onGoToNextStep()
}