package com.github.khangnt.mcp.ui.common

import android.support.v7.widget.RecyclerView
import android.view.View
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

interface AdapterModel

interface HasIdLong {
    val idLong: Long
}

interface HasIdString {
    val idString: String
}

abstract class CustomViewHolder<in T : AdapterModel>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(model: T, pos: Int)
}

class IdGenerator private constructor(initValue: Int) {
    companion object {
        const val SCOPE_GLOBAL = "Global"
        const val DEFAULT_INIT_VALUE = 100_000

        private val sInstances = mutableMapOf<String, IdGenerator>()

        fun scope(scopeName: String, initValue: Int = 100_000): IdGenerator {
            synchronized(sInstances) {
                return sInstances.getOrPut(scopeName, { IdGenerator(initValue) })
            }
        }
    }

    private val atomicInt = AtomicInteger(initValue)
    private val map = mutableMapOf<String, Int>()

    fun idFor(stringValue: String): Int {
        return map.getOrPut(stringValue) {
            atomicInt.getAndIncrement()
        }
    }
}