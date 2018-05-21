package com.github.khangnt.mcp.ui.jobmaker.selectformat

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.ConvertCommand
import com.github.khangnt.mcp.EditCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.HeaderModel
import com.github.khangnt.mcp.ui.common.ItemHeaderViewHolder
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.decorator.ItemOffsetDecoration
import com.github.khangnt.mcp.ui.jobmaker.JobMakerViewModel
import com.github.khangnt.mcp.ui.jobmaker.StepFragment
import com.github.khangnt.mcp.ui.jobmaker.selectoutput.ChooseOutputViewModel
import com.github.khangnt.mcp.util.getViewModel
import com.github.khangnt.mcp.util.toast
import kotlinx.android.synthetic.main.fragment_choose_command.*

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
            withModel<ConvertCommandModel> {
                ItemConvertCommandViewHolder.Factory {
                    onClick = { onSelectConvertCommand(it) }
                }
            }
            withModel<EditCommandModel> {
                ItemEditCommandViewHolder.Factory {
                    onClick = { onSelectEditCommand(it) }
                }
            }
        }.build()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_choose_command, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ItemOffsetDecoration(view.context)
                .setHorizontalSpace(R.dimen.margin_normal)
                .setVerticalSpace(R.dimen.margin_small)
                .applyTo(recyclerView)
        val spanCount = resources.getInteger(R.integer.spanCount)
        val lm = GridLayoutManager(view.context, spanCount)
        lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemData(position)) {
                    is HeaderModel -> spanCount
                    is EditCommandModel -> spanCount
                    else -> 1
                }
            }
        }
        recyclerView.layoutManager = lm
        recyclerView.adapter = adapter

        jobMakerViewModel.getSelectedFiles().observe {
            adapter.setData(makeAdapterData(it.size))
        }
    }

    private fun makeAdapterData(selectedFileCount: Int): List<AdapterModel> {
        val adapterModels = mutableListOf<AdapterModel>()
        adapterModels.add(HeaderModel(getString(R.string.header_convert_to_other_format)))
        adapterModels.addAll(ConvertCommand.values().map {
            ConvertCommandModel(it, selectedFileCount > 0)
        })

        adapterModels.add(HeaderModel(getString(R.string.header_other_feature)))
        EditCommand.values().forEach {
            val range = it.minInputCount..it.maxInputCount
            adapterModels.add(EditCommandModel(it, selectedFileCount in range))
        }
        return adapterModels
    }

    private fun onSelectConvertCommand(convertCommandModel: ConvertCommandModel) {
        if (!convertCommandModel.enabled) {
            toast(resources.getQuantityString(R.plurals.select_at_least, 1, 1))
            return
        }
        jobMakerViewModel.setSelectedCommand(convertCommandModel.command)
        jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_CONFIGURE_COMMAND)
    }

    private fun onSelectEditCommand(editCommandModel: EditCommandModel) {
        if (!editCommandModel.enabled) {
            val selectedFileCount = checkNotNull(jobMakerViewModel.getSelectedFiles().value).size
            if (selectedFileCount < editCommandModel.editCommand.minInputCount) {
                val min = editCommandModel.editCommand.minInputCount
                toast(resources.getQuantityString(R.plurals.select_at_least, min, min))
            } else {
                val max = editCommandModel.editCommand.maxInputCount
                toast(resources.getQuantityString(R.plurals.select_at_most, max, max))
            }
            return
        }

        jobMakerViewModel.setSelectedCommand(editCommandModel.editCommand)
        jobMakerViewModel.setCurrentStep(JobMakerViewModel.STEP_CONFIGURE_COMMAND)
    }

    override fun onGoToNextStep() {
        toast(R.string.message_please_select_an_option)
    }

}