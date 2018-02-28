package com.github.khangnt.mcp.ui.jobmanager

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.annotation.JobStatus.*
import com.github.khangnt.mcp.job.jobComparator
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.HeaderModel
import com.github.khangnt.mcp.ui.common.ItemHeaderViewHolder
import com.github.khangnt.mcp.ui.common.MixAdapter
import com.github.khangnt.mcp.ui.decorator.ItemOffsetDecoration
import com.github.khangnt.mcp.util.getSpanCount
import com.github.khangnt.mcp.view.RecyclerViewGroupState
import com.github.khangnt.mcp.worker.ConverterService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_job_manager.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

class JobManagerFragment : BaseFragment() {
    private val jobManager = SingletonInstances.getJobManager()
    private val runningHeaderModel = RunningHeaderModel("Running",
            jobManager.getLiveLogObservable(), { disposeOnPaused() })
    private val preparingHeaderModel = HeaderModel("Preparing")
    private val readyHeaderModel = HeaderModel("Ready")
    private val pendingHeaderModel = HeaderModel("Pending")
    private val finishedHeaderModel = HeaderModel("Finished")

    private lateinit var adapter: MixAdapter
    private lateinit var recyclerViewGroupState: RecyclerViewGroupState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        context!!.startService(Intent(context!!, ConverterService::class.java))
        adapter = MixAdapter.Builder(idGeneratorScope = "JobManagerFragment")
                .register(HeaderModel::class.java, ItemHeaderViewHolder.Factory)
                .register(RunningHeaderModel::class.java, ItemHeaderRunningViewHolder.Factory)
                .register(JobModel::class.java, ItemJobViewHolder.Factory)
                .build()
        recyclerViewGroupState = RecyclerViewGroupState().setRetryFunc(this::loadData)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_job_manager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActivitySupportActionBar(toolbar)
        ItemOffsetDecoration(context!!)
                .setHorizontalSpace(R.dimen.margin_normal)
                .setVerticalSpace(R.dimen.margin_small)
                .applyTo(recyclerViewGroup.getRecyclerView())
        val itemJobMinWidth = resources.getDimensionPixelOffset(R.dimen.item_job_min_width)
        val columnSpace = resources.getDimensionPixelOffset(R.dimen.margin_normal)
        val spanCount = getSpanCount(itemJobMinWidth, columnSpace)
        val lm = GridLayoutManager(view.context, spanCount)
        lm.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getData(position) is HeaderModel) spanCount else 1
            }
        }
        recyclerViewGroupState.bind(recyclerViewGroup, adapter, lm)
        /*
         * Experiment to fix issue: https://issuetracker.google.com/issues/37132919
         * The reason maybe adapter's data refresh during configuration change.
         */
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        recyclerViewGroupState.loading()
        jobManager.getJob(RUNNING, PREPARING, READY, PENDING, COMPLETED, FAILED)
                .throttleLast(400, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.computation())
                .doOnNext { Collections.sort(it, jobComparator) }
                .map { originalList ->
                    val listModels = mutableListOf<AdapterModel>()
                    var addedFinishedHeader = false
                    var previousStatus: Int? = null
                    originalList.forEach { item ->
                        if (item.status != previousStatus) {
                            previousStatus = item.status
                            if (item.status == RUNNING) {
                                listModels.add(runningHeaderModel)
                            } else if (item.status == PREPARING) {
                                listModels.add(preparingHeaderModel)
                            } else if (item.status == READY) {
                                listModels.add(readyHeaderModel)
                            } else if (item.status == PENDING) {
                                listModels.add(pendingHeaderModel)
                            } else if (!addedFinishedHeader) {
                                addedFinishedHeader = true
                                listModels.add(finishedHeaderModel)
                            }
                        }
                        listModels.add(JobModel(item))
                    }
                    return@map listModels
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    adapter.setData(list)
                    recyclerViewGroupState.checkData(list)
                }, { error ->
                    Timber.e(error, "Load job list failed")
                    adapter.setData(emptyList())
                    recyclerViewGroupState.error(error.message)
                })
                .disposeOnPaused(tag = "loadData")
    }

}