package com.github.khangnt.mcp.view

import android.content.Context
import android.support.annotation.MainThread
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.getKnownReasonOf
import com.github.khangnt.mcp.ui.common.Status
import kotlinx.android.synthetic.main.view_recyclerview_container.view.*

class RecyclerViewContainer @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    lateinit var onRefreshListener: (() -> Boolean)

    var showReloadOnEmpty: Boolean = true
    var loadingText: CharSequence? = null
    var emptyText: CharSequence? = null

    private var idleStatusPending = false
    private val updateIdleStatusRunnable = Runnable {
        if (idleStatusPending) {
            idleStatusPending = false
            setRefreshing(false)
            messageTv.text = emptyText
            reloadButton.visibility = View.VISIBLE
        }
    }

    init {
        LayoutInflater.from(context).inflate(
                R.layout.view_recyclerview_container, this, true)
        if (attrs != null) {
            val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.RecyclerViewContainer,
                    defStyleAttr, 0)
            try {
                val rvPaddingVertical = ta.getDimensionPixelOffset(
                        R.styleable.RecyclerViewContainer_rv_paddingVertical, 0)
                val rvPaddingHorizontal = ta.getDimensionPixelOffset(
                        R.styleable.RecyclerViewContainer_rv_paddingHorizontal, 0)
                val rvClipToPadding = ta.getBoolean(
                        R.styleable.RecyclerViewContainer_rv_clipToPadding, false)
                recyclerView.setPadding(rvPaddingHorizontal, rvPaddingVertical, rvPaddingHorizontal,
                        rvPaddingVertical)
                recyclerView.clipToPadding = rvClipToPadding

                showReloadOnEmpty = ta.getBoolean(R.styleable.RecyclerViewContainer_rv_showReloadOnEmpty,
                        true)
                loadingText = ta.getText(R.styleable.RecyclerViewContainer_rv_loadingText)
                emptyText = ta.getText(R.styleable.RecyclerViewContainer_rv_emptyText)
            } finally {
                ta.recycle()
            }
        }
        swipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green,
                R.color.yellow, R.color.blue)
        swipeRefreshLayout.setOnRefreshListener {
            if (!onRefreshListener.invoke()) setRefreshing(false)
        }
        reloadButton.setOnClickListener {
            if (onRefreshListener.invoke()) setRefreshing(true)
        }
    }

    fun setSwipeRefreshEnabled(enabled: Boolean) {
        swipeRefreshLayout.isEnabled = enabled
    }

    fun getRecyclerView(): RecyclerView = recyclerView

    fun setShowEmptyState(show: Boolean) {
        if (show) {
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            emptyStateLayout.visibility = View.GONE
        }
    }

    fun setStatus(status: Status) {
        idleStatusPending = status == Status.Idle
        when (status) {
            Status.Idle -> {
                postDelayed(updateIdleStatusRunnable, 100)
                if (!showReloadOnEmpty) {
                    reloadButton.visibility = View.GONE
                }
            }
            Status.Loading -> {
                setRefreshing(true)
                messageTv.text = loadingText
                reloadButton.visibility = View.GONE
            }
            is Status.Error -> {
                val errorMessage = getKnownReasonOf(status.throwable, context,
                        "${status.throwable.message}")
                setRefreshing(false)
                messageTv.text = errorMessage
                reloadButton.visibility = View.VISIBLE

                if (!status.handled && emptyStateLayout.visibility == View.GONE) {
                    Snackbar.make(this, errorMessage, Snackbar.LENGTH_LONG).show()
                }
                status.handled = true
            }
        }
    }

    @MainThread
    private fun setRefreshing(refreshing: Boolean) {
        if (swipeRefreshLayout.isRefreshing != refreshing) {
            swipeRefreshLayout.isRefreshing = refreshing
        }
    }

    fun promptRefresh(message: String, onReloadClick: () -> Unit) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.reload, { onReloadClick() })
                .show()
    }

}