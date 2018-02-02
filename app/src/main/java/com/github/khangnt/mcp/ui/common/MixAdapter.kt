package com.github.khangnt.mcp.ui.common

import android.os.Looper
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.*
import kotlin.collections.ArrayList


typealias ViewHolderFactory = (inflater: LayoutInflater, parent: ViewGroup) -> CustomViewHolder<*>

/**
 * Created by Khang NT on 1/29/18.
 * Email: khang.neon.1997@gmail.com
 */

class MixAdapter(
        private val idGenerator: IdGenerator,
        private val itemTypes: Map<Class<out AdapterModel>, Int>,
        private val viewHolderFactories: Map<Int, ViewHolderFactory>
) : RecyclerView.Adapter<CustomViewHolder<*>>() {

    private val itemDataList = mutableListOf<AdapterModel>()

    init {
        // has stable ids as default
        setHasStableIds(true)
    }

    fun setData(items: List<AdapterModel>, diffResult: DiffUtil.DiffResult? = null) {
        check(Looper.myLooper() == Looper.getMainLooper(), { "Must call on main thread" })
        itemDataList.clear()
        itemDataList.addAll(items)
        if (diffResult !== null) {
            diffResult.dispatchUpdatesTo(this)
        } else {
            notifyDataSetChanged()
        }
    }

    fun getDataSnapshot(): ArrayList<AdapterModel> = ArrayList(itemDataList)

    override fun getItemCount(): Int = itemDataList.size

    override fun getItemViewType(position: Int): Int {
        val dataClass = getData(position).javaClass
        return itemTypes[dataClass]
                ?: throw IllegalStateException("Unknown item view type for $dataClass")
    }

    override fun getItemId(position: Int): Long {
        val data = getData(position)
        return (data as? HasIdLong)?.idLong
                ?: (data as? HasIdString)?.run { idGenerator.idFor(idString).toLong() }
                ?: super.getItemId(position)
    }

    fun getData(pos: Int): AdapterModel = itemDataList[pos]

    fun indexOf(model: AdapterModel): Int = itemDataList.indexOf(model)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder<*> {
        return viewHolderFactories[viewType]?.invoke(LayoutInflater.from(parent.context), parent)
                ?: throw IllegalArgumentException("Unknown view type $viewType")
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: CustomViewHolder<*>, position: Int) {
        val data = getData(position)
        (holder as CustomViewHolder<AdapterModel>).bind(data, position)
    }

    class Builder(
            idGeneratorScope: String = IdGenerator.SCOPE_GLOBAL,
            idGeneratorInit: Int = IdGenerator.DEFAULT_INIT_VALUE
    ) {
        private val idGenerator = IdGenerator.scope(idGeneratorScope, idGeneratorInit)
        private var itemTypeCounter = 0
        private val itemTypes = mutableMapOf<Class<out AdapterModel>, Int>()
        private val viewHolderFactories = mutableMapOf<Int, ViewHolderFactory>()

        fun register(dataClass: Class<out AdapterModel>, viewHolderFactory: ViewHolderFactory): Builder {
            val itemTypeId = itemTypeCounter++
            itemTypes[dataClass] = itemTypeId
            viewHolderFactories[itemTypeId] = viewHolderFactory
            return this
        }

        fun build(): MixAdapter = MixAdapter(idGenerator,
                Collections.unmodifiableMap(itemTypes.toMutableMap()),
                Collections.unmodifiableMap(viewHolderFactories.toMutableMap()))
    }
}

