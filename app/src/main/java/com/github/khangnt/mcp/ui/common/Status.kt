package com.github.khangnt.mcp.ui.common

@Suppress("unused")
sealed class Status {
    object Idle : Status()
    object Loading : Status()
    data class Error(val throwable: Throwable, var handled: Boolean = false): Status()
}