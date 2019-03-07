package com.github.khangnt.mcp.ui.common

import android.support.annotation.LayoutRes
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.util.checkMainThread
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

/**
 * Created by Khang NT on 3/23/18.
 * Email: khang.neon.1997@gmail.com
 */

interface ViewHolderFactory {
    @get:LayoutRes val layoutRes: Int
    fun create(itemView: View): CustomViewHolder<*>
}

data class ItemType(
        val viewType: Int,
        val viewHolderFactory: ViewHolderFactory
)

class MixAdapter(
        private val mapModelClassItemType: Map<Class<out AdapterModel>, ItemType>
) : RecyclerView.Adapter<CustomViewHolder<*>>(), ItemTouchHelperAdapter {

    private var layoutInflater: LayoutInflater? = null
    private val itemDataList = mutableListOf<AdapterModel>()
    private val mapViewTypeItemType: SparseArray<ItemType>

    init {
        // has stable ids as default
        setHasStableIds(true)
        mapViewTypeItemType = SparseArray()
        mapModelClassItemType.forEach { entry ->
            mapViewTypeItemType.put(entry.value.viewType, entry.value)
        }
    }

    fun setData(items: List<AdapterModel>, diffResult: DiffUtil.DiffResult? = null) {
        checkMainThread("setData")
        itemDataList.clear()
        itemDataList.addAll(items)
        if (diffResult !== null) {
            diffResult.dispatchUpdatesTo(this)
        } else {
            notifyDataSetChanged()
        }
    }

    fun getItemDataList(): List<AdapterModel> = itemDataList.toList() // make a copy

    fun getItemData(position: Int) = itemDataList.get(position)

    override fun getItemCount() = itemDataList.size

    override fun getItemViewType(position: Int): Int {
        val dataClass = itemDataList[position].javaClass
        return mapModelClassItemType[dataClass]?.viewType
                ?: throw IllegalStateException("Unknown item view type for $dataClass")
    }

    override fun getItemId(position: Int): Long {
        val data = itemDataList[position]
        return (data as? HasIdLong)?.idLong ?: super.getItemId(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder<*> {
        if (layoutInflater === null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        val itemType = mapViewTypeItemType.get(viewType)
                ?: throw IllegalArgumentException("Unknown view type $viewType")
        val layoutRes = itemType.viewHolderFactory.layoutRes
        val itemView = layoutInflater!!.inflate(layoutRes, parent, false)
        return itemType.viewHolderFactory.create(itemView)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: CustomViewHolder<*>, position: Int) {
        (holder as CustomViewHolder<AdapterModel>).bind(itemDataList[position], position)
    }

    override fun onViewAttachedToWindow(holder: CustomViewHolder<*>) {
        super.onViewAttachedToWindow(holder)
        holder.onAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: CustomViewHolder<*>) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetachedFromWindow()
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        itemDataList.add(toPosition, itemDataList.removeAt(fromPosition))
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
        itemDataList.removeAt(position)
        notifyItemRemoved(position)
    }

    class Builder(block: Builder.() -> Unit) {
        val itemTypes = mutableMapOf<Class<out AdapterModel>, ItemType>()

        init {
            block()
        }

        inline fun <reified T : AdapterModel> withModel(block: () -> ViewHolderFactory) {
            itemTypes[T::class.java] = ItemType(itemTypes.size, block())
        }

        fun build(): MixAdapter = MixAdapter(itemTypes.toMap())
    }
}