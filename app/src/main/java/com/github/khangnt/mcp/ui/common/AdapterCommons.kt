package com.github.khangnt.mcp.ui.common

import android.support.v7.widget.RecyclerView
import android.view.View
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

interface AdapterModel

interface HasIdLong {
    val idLong: Long
}

abstract class CustomViewHolder<in T : AdapterModel>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(model: T, pos: Int)

    open fun onAttachedToWindow() = Unit

    open fun onDetachedFromWindow() = Unit
}

object IdGenerator {
    private val atomicInt = AtomicLong(999999)
    private val map = ConcurrentHashMap<String, Long>()

    fun idFor(stringValue: String): Long {
        return map.getOrPut(stringValue) { atomicInt.getAndIncrement() }
    }
}