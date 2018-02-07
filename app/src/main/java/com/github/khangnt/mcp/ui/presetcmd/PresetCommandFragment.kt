package com.github.khangnt.mcp.ui.presetcmd

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.PresetCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.TYPE_AUDIO
import com.github.khangnt.mcp.TYPE_VIDEO
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.HeaderModel
import com.github.khangnt.mcp.ui.common.ItemHeaderViewHolder
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.decorator.ItemOffsetDecoration
import com.github.khangnt.mcp.util.getSpanCount
import kotlinx.android.synthetic.main.fragment_preset_command.*

/**
 * Created by Khang NT on 1/6/18.
 * Email: khang.neon.1997@gmail.com
 */

class PresetCommandFragment : BaseFragment() {

    private val audioEncodingHeader = HeaderModel("Audio encoding")
    private val videoEncodingHeader = HeaderModel("Video encoding")

    private lateinit var adapter: MixAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = MixAdapter.Builder()
                .register(PresetCommandModel::class.java, ItemPresetCommandViewHolder.FACTORY)
                .register(HeaderModel::class.java, ItemHeaderViewHolder.Factory)
                .build()

        val data: MutableList<AdapterModel> = mutableListOf<AdapterModel>()
        val audioPresetCmds = PresetCommand.values().filter { it.type == TYPE_AUDIO }
        val videoPresetCmds = PresetCommand.values().filter { it.type == TYPE_VIDEO }
        if (audioPresetCmds.isNotEmpty()) {
            data.add(audioEncodingHeader)
            data.addAll(audioPresetCmds.map { PresetCommandModel(it) })
        }
        if (videoPresetCmds.isNotEmpty()) {
            data.add(videoEncodingHeader)
            data.addAll(videoPresetCmds.map { PresetCommandModel(it) })
        }
        adapter.setData(data)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_preset_command, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActivitySupportActionBar(toolbar)

        val itemOffsetDecoration = ItemOffsetDecoration(view.context)
                .setHorizontalSpace(R.dimen.margin_normal)
                .setVerticalSpace(R.dimen.margin_small)
                .applyTo(recyclerViewGroup.getRecyclerView())
        val itemMinWidth = resources.getDimensionPixelOffset(R.dimen.item_job_min_width)
        val spanCount = getSpanCount(itemMinWidth, itemOffsetDecoration.horizontalSpace)
        val lm = GridLayoutManager(view.context, spanCount)
        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                    if (adapter.getData(position) is HeaderModel) spanCount else 1
        }

        recyclerViewGroup.getRecyclerView().layoutManager = lm
        recyclerViewGroup.getRecyclerView().adapter = adapter
        recyclerViewGroup.successHasData()
    }

}
