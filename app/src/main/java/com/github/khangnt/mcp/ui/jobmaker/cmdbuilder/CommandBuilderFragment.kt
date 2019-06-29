package com.github.khangnt.mcp.ui.jobmaker.cmdbuilder

import com.github.khangnt.mcp.ui.BaseFragment
import java.util.*

/**
 * Created by Khang NT on 4/9/18.
 * Email: khang.neon.1997@gmail.com
 */

abstract class CommandBuilderFragment : BaseFragment() {

    companion object {
        const val ARG_INPUT_FILE_URIS = "inputFileUris"
    }

    protected val inputFileUris: List<String> by lazy {
        Collections.unmodifiableList(arguments?.getStringArrayList(ARG_INPUT_FILE_URIS)
                ?: throw IllegalStateException("Missing argument $ARG_INPUT_FILE_URIS"))
    }

    abstract fun validateConfig(onSuccess: (CommandConfig) -> Unit)

}