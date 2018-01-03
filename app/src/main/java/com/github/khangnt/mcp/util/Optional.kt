package com.github.khangnt.mcp.util

/**
 * Created by Khang NT on 1/3/18.
 * Email: khang.neon.1997@gmail.com
 */

data class Optional<out T>(val value: T?) {
    companion object {
        private val NULL = Optional(null)

        fun <T> absent(): Optional<T> = NULL
    }
}

fun <T> T?.asOptional(): Optional<T> = Optional(this)