package com.github.khangnt.mcp.job

import com.github.khangnt.mcp.annotation.MediaFormat

/**
 * Created by Khang NT on 12/30/17.
 * Email: khang.neon.1997@gmail.com
 */

data class Command(
        val inputs: List<String>,
        val output: String,
        @MediaFormat
        val outputFormat: String,
        val args: String,
        val environmentVars: Map<String, String>
) {
    init {
        if (inputs.isEmpty()) {
            throw IllegalArgumentException("`inputs` must not empty")
        }
    }
}
