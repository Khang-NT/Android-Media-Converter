package com.github.khangnt.mcp.ui.jobmaker

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.ConvertCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.TYPE_AUDIO
import com.github.khangnt.mcp.TYPE_VIDEO
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.HeaderModel
import com.github.khangnt.mcp.ui.common.ItemHeaderViewHolder
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.decorator.ItemOffsetDecoration
import com.github.khangnt.mcp.ui.presetcmd.ItemPresetCommandViewHolder
import com.github.khangnt.mcp.ui.presetcmd.PresetCommandModel
import com.github.khangnt.mcp.util.getSpanCount
import com.github.khangnt.mcp.util.getViewModel
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_preset_command.*

/**
 * Created by Khang NT on 4/7/18.
 * Email: khang.neon.1997@gmail.com
 */

class ChooseCommandFragment : StepFragment() {

    /** Get shared view model via host activity **/
    private val jobMakerViewModel by lazy { requireActivity().getViewModel<JobMakerViewModel>() }

    private val adapter: MixAdapter by lazy {
        MixAdapter.Builder {
            withModel<HeaderModel> { ItemHeaderViewHolder.Factory() }
            withModel<PresetCommandModel> {
                ItemPresetCommandViewHolder.Factory {
                    onClickListener = this@ChooseCommandFragment::onPresetClick
                }
            }
        }.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val audioEncodingHeader = HeaderModel(getString(R.string.header_audio_encoding))
        val videoEncodingHeader = HeaderModel(getString(R.string.header_video_encoding))
        val data: MutableList<AdapterModel> = mutableListOf()
        val audioPresetCmds = ConvertCommand.values().filter { it.type == TYPE_AUDIO }
        val videoPresetCmds = ConvertCommand.values().filter { it.type == TYPE_VIDEO }
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

//        textView.text = """- Show list preset command
//- A preset command can be disabled if number inputs file is not suitable
//- Select a enabled preset command to next""".trimIndent()

//        textView.setOnClickListener {
//            jobMakerViewModel.setSelectedCommand(ConvertCommand.CONVERT_MP3)
//            jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_CONFIGURE_COMMAND)
//        }


        val itemOffsetDecoration = ItemOffsetDecoration(view.context)
                .setHorizontalSpace(R.dimen.margin_normal)
                .setVerticalSpace(R.dimen.margin_small)
                .applyTo(presetList)
        val itemMinWidth = resources.getDimensionPixelOffset(R.dimen.item_preset_cmd_min_width)
        val spanCount = getSpanCount(itemMinWidth, itemOffsetDecoration.horizontalSpace)
        val lm = GridLayoutManager(view.context, spanCount)
        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemData(position) is HeaderModel) spanCount else 1
            }
        }

        presetList.layoutManager = lm
        presetList.adapter = adapter

    }

    private fun onPresetClick(convertCommand: ConvertCommand) {
        jobMakerViewModel.setSelectedCommand(ConvertCommand.CONVERT_MP3)
        jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_CONFIGURE_COMMAND)
    }

    override fun onGoToNextStep() {
        toast("Select a command to continue (click text view)")
    }

}