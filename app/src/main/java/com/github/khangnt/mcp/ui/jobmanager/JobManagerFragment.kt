package com.github.khangnt.mcp.ui.jobmanager

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.HeaderModel
import com.github.khangnt.mcp.ui.common.ItemHeaderViewHolder
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.common.Status
import com.github.khangnt.mcp.ui.decorator.ItemOffsetDecoration
import com.github.khangnt.mcp.ui.jobmaker.JobMakerActivity
import com.github.khangnt.mcp.util.getSpanCount
import kotlinx.android.synthetic.main.fragment_job_manager.*

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

class JobManagerFragment : BaseFragment() {

    private val viewModel by lazy { getViewModel<JobManagerViewModel>() }
    private val adapter: MixAdapter by lazy {
        MixAdapter.Builder {
            withModel<LiveHeaderModel> { ItemLiveHeaderViewHolder.Factory() }
            withModel<HeaderModel> { ItemHeaderViewHolder.Factory() }
            withModel<JobModel> { ItemJobViewHolder.Factory() }
        }.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SingletonInstances.getJobWorkerMangager().maybeLaunchWorker()

        val latestStatus = viewModel.getStatus().value
        val latestJobs = viewModel.getAdapterModel().value.orEmpty()
        if (latestStatus is Status.Error // auto reload
                || latestStatus != Status.Loading && latestJobs.isEmpty()) {
            viewModel.reload()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_job_manager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActivitySupportActionBar(toolbar)
        with(recyclerViewContainer) {
            onRefreshListener = { viewModel.reload(); true }
            ItemOffsetDecoration(context!!)
                    .setHorizontalSpace(R.dimen.margin_normal)
                    .setVerticalSpace(R.dimen.margin_small)
                    .applyTo(getRecyclerView())
            val itemJobMinWidth = resources.getDimensionPixelOffset(R.dimen.item_job_min_width)
            val columnSpace = resources.getDimensionPixelOffset(R.dimen.margin_normal)
            val spanCount = getSpanCount(itemJobMinWidth, columnSpace)
            val lm = GridLayoutManager(view.context, spanCount)
            lm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val data = adapter.getItemData(position)
                    return if (data is HeaderModel || data is LiveHeaderModel) spanCount else 1
                }
            }

            getRecyclerView().layoutManager = lm
            getRecyclerView().adapter = adapter
        }
        viewModel.getAdapterModel().observe { adapterModels ->
            adapter.setData(adapterModels)
            recyclerViewContainer.setShowEmptyState(adapterModels.isEmpty())
        }
        viewModel.getStatus().observe { status ->
            recyclerViewContainer.setStatus(status)
        }

        createJobFab.setOnClickListener {
            startActivity(Intent(requireContext(), JobMakerActivity::class.java))
        }
    }

}