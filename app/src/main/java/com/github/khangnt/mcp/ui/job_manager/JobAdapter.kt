package com.github.khangnt.mcp.ui.job_manager

import android.os.Looper
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.*
import com.github.khangnt.mcp.util.toImmutable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

private const val ITEM_ID_RUNNING_HEADER = 0
private const val ITEM_ID_HEADER = 1
private const val ITEM_ID_JOB = 2

class JobAdapter(
        private val outputSizeObservable: Observable<String>
) : RecyclerView.Adapter<CustomViewHolder<*>>() {
    private var compositeDisposable: CompositeDisposable? = null
    private var itemDataList: List<AdapterModel> = emptyList()
    private var layoutInflater: LayoutInflater? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        super.onDetachedFromRecyclerView(recyclerView)
        compositeDisposable?.dispose()
    }

    fun setData(items: List<AdapterModel>, diffResult: DiffUtil.DiffResult? = null) {
        check(Looper.myLooper() == Looper.getMainLooper(), { "Must call on main thread" })
        itemDataList = items.toImmutable()
        if (diffResult !== null) {
            diffResult.dispatchUpdatesTo(this)
        } else {
            notifyDataSetChanged()
        }
    }

    override fun getItemId(position: Int): Long {
        (itemDataList[position] as? HasIdModel)?.let { return it.modelId }
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        itemDataList[position].let { item ->
            return when (item) {
                is HeaderModel -> {
                    if (item is RunningHeaderModel) {
                        ITEM_ID_RUNNING_HEADER
                    } else {
                        ITEM_ID_HEADER
                    }
                }
                is JobModel -> ITEM_ID_JOB
                else -> throw IllegalArgumentException("Unknown item type: ${item.javaClass}")
            }
        }
    }

    private fun getLayoutInflater(view: View): LayoutInflater {
        return layoutInflater ?: LayoutInflater.from(view.context).apply { layoutInflater = this }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder<*> {
        return when (viewType) {
            ITEM_ID_JOB -> {
                ItemJobViewHolder(getLayoutInflater(parent)
                        .inflate(R.layout.item_job, parent, false))
            }
            ITEM_ID_HEADER -> {
                ItemHeaderViewHolder(getLayoutInflater(parent)
                        .inflate(R.layout.item_header, parent, false))
            }
            ITEM_ID_RUNNING_HEADER -> {
                ItemHeaderRunningViewHolder(
                        itemView = getLayoutInflater(parent)
                                .inflate(R.layout.item_header, parent, false),
                        outputSize = outputSizeObservable,
                        compositeDisposable = compositeDisposable!!
                )
            }
            else -> throw IllegalArgumentException("Unknown item type $viewType")
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder<*>, position: Int) {
        val itemData = itemDataList[position]
        if (holder is ItemHeaderViewHolder) {
            holder.bind(itemData as HeaderModel, position)
        } else if (holder is ItemJobViewHolder) {
            holder.bind(itemData as JobModel, position)
        }
    }

    override fun getItemCount(): Int = itemDataList.size

}