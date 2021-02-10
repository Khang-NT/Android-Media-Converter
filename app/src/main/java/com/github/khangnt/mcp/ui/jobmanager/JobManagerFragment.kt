package com.github.khangnt.mcp.ui.jobmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.view.menu.MenuBuilder
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.khangnt.mcp.Gradient
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.SingletonInstances
import com.github.khangnt.mcp.SingletonInstances.Companion.getSharedPrefs
import com.github.khangnt.mcp.ui.BaseFragment
import com.github.khangnt.mcp.ui.common.*
import com.github.khangnt.mcp.ui.decorator.ItemOffsetDecoration
import com.github.khangnt.mcp.ui.jobmaker.JobMakerActivity
import com.github.khangnt.mcp.util.catchAll
import com.github.khangnt.mcp.util.getSpanCount
import com.github.khangnt.mcp.util.getViewModel
import com.github.khangnt.mcp.util.toast
import com.github.khangnt.mcp.worker.makeWorkingPaths
import com.google.android.gms.ads.AdRequest
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_job_manager.*
import java.util.*

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

    private lateinit var adRequest: AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SingletonInstances.getJobWorkerManager().maybeLaunchWorker()

        val latestStatus = viewModel.getStatus().value
        val latestJobs = viewModel.getAdapterModel().value.orEmpty()
        if (latestStatus is Status.Error // auto reload
                || latestStatus != Status.Loading && latestJobs.isEmpty()) {
            viewModel.reload()
        }

        if (getSharedPrefs().enabledAds) {
            adRequest = AdRequest.Builder().build()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_job_manager, container, false).apply {
        setActivitySupportActionBar(findViewById(R.id.toolbar))
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collapsingToolbar.title = getString(R.string.nav_job_manager)
        val gradient = Gradient.values()[Random().nextInt(Gradient.values().size)]
        collapsingToolbar.contentScrim = gradient.getDrawable()
        collapsingToolbar.statusBarScrim = gradient.getDrawable()
        val scrollRange by lazy { appBarLayout.totalScrollRange }
        val drawable = toolbar.getTag(R.id.toolbar_slide_drawable) as DrawerArrowDrawable
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset: Int ->
            if (scrollRange + offset == 0) {
                drawable.color = Color.WHITE
                toolbar.overflowIcon?.let { DrawableCompat.setTint(it, Color.WHITE) }
            } else if (offset == 0) {
                drawable.color = Color.BLACK
                toolbar.overflowIcon?.let { DrawableCompat.setTint(it, Color.BLACK) }
            }
        })


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

        recyclerViewContainer.getRecyclerView().onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (velocityY < 0) {
                    createJobFab.show()
                } else if (velocityY > 0
                        && viewModel.getAdapterModel().value.orEmpty().isNotEmpty()) {
                    createJobFab.hide()
                }
                return false
            }

        }

        if (getSharedPrefs().enabledAds) {
            adView.loadAd(adRequest)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.fragment_job_manager, menu)
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_clear_finished_jobs -> {
                AlertDialog.Builder(context!!)
                        .setMessage(R.string.message_clear_finished_jobs)
                        .setPositiveButton(R.string.action_ok) { _, _ ->
                            SingletonInstances.getJobWorkerManager().clearFinishedJobs()
                            // delete all logs
                            catchAll {
                                makeWorkingPaths(context!!).getAllLogFiles()
                                        .forEach { log -> log.delete() }
                            }
                            toast(getString(R.string.message_cleared_all_finished_jobs))
                        }
                        .setNegativeButton(R.string.action_cancel, null)
                        .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}