package com.github.khangnt.mcp.ui.presetcmd

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.PresetCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.TYPE_AUDIO
import com.github.khangnt.mcp.TYPE_VIDEO
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.MainActivity
import com.github.khangnt.mcp.ui.common.*
import com.github.khangnt.mcp.ui.decorator.ItemOffsetDecoration
import com.github.khangnt.mcp.util.getSpanCount
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_preset_command.*

/**
 * Created by Khang NT on 1/6/18.
 * Email: khang.neon.1997@gmail.com
 */

private const val RC_CONVERT_ACTIVITY = 9

class PresetCommandFragment : BaseFragment() {

    private val audioEncodingHeader = HeaderModel("Audio encoding")
    private val videoEncodingHeader = HeaderModel("Video encoding")

    private lateinit var adapter: MixAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = MixAdapter.Builder()
                .register(PresetCommandModel::class.java, presetCommandViewHolderFactory)
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

    private val presetCommandViewHolderFactory: ViewHolderFactory = { inflater, parent ->
        val itemView = inflater.inflate(R.layout.item_preset_command, parent, false)
        val onItemClick = { presetCommand: PresetCommand ->
            toast(presetCommand.titleRes)
            val intent = ConvertActivity.launchIntent(context!!, presetCommand.ordinal)
            startActivityForResult(intent, RC_CONVERT_ACTIVITY)
        }
        ItemPresetCommandViewHolder(itemView, onItemClick)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == RC_CONVERT_ACTIVITY) {
            Snackbar.make(recyclerViewGroup.rootView, R.string.add_job_message, Snackbar.LENGTH_SHORT)
                    .setAction(getString(R.string.ac_view), {
                        startActivity(MainActivity.openJobManagerIntent(it.context))
                    })
                    .show()
        }
    }
}
