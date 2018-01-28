package com.github.khangnt.mcp.ui.common

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

interface AdapterModel

interface HasIdModel {
    val modelId: Long
}

abstract class CustomViewHolder<in T : AdapterModel>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(model: T, pos: Int)
}

class IdGenerator private constructor(initValue: Int = 100_000) {
    companion object {
        private val sInstances = mutableMapOf<String, IdGenerator>()

        fun scope(scopeName: String): IdGenerator {
            synchronized(sInstances) {
                return sInstances.getOrPut(scopeName, { IdGenerator() })
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

fun diffCallback(oldList: List<AdapterModel>, newList: List<AdapterModel>): DiffUtil.Callback {
    return object : DiffUtil.Callback() {
        override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
        ): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem === newItem ||
                    (oldItem is HasIdModel && newItem is HasIdModel && oldItem.modelId == newItem.modelId)
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
        ): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
    }
}