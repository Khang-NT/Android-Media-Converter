package com.github.khangnt.mcp.view

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.util.gone
import com.github.khangnt.mcp.util.visible
import kotlinx.android.synthetic.main.view_recycler_view_group.view.*
import java.lang.ref.WeakReference


const val STATE_EMPTY = 0
const val STATE_LOADING = 1
const val STATE_ERROR = 2
const val STATE_SUCCESS_HAS_DATA = 3

/**
 * Mutable class, contains State of RecyclerViewGroup
 */
class RecyclerViewGroupState {
    private var state: Int = STATE_SUCCESS_HAS_DATA
    private var retryFunc: (() -> Unit)? = null
    private var errorReason: String? = null
    private var recyclerViewGroupWeakRef: WeakReference<RecyclerViewGroup>? = null

    fun bind(
            recyclerViewGroup: RecyclerViewGroup,
            adapter: RecyclerView.Adapter<*>?,
            layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(recyclerViewGroup.context)
    ) {
        recyclerViewGroupWeakRef = WeakReference(recyclerViewGroup)
        recyclerViewGroup.setRetryFunc(retryFunc)
        recyclerViewGroup.getRecyclerView().layoutManager = layoutManager
        recyclerViewGroup.getRecyclerView().swapAdapter(adapter, true)
        updateState()
    }

    fun setRetryFunc(func: (() -> Unit)?): RecyclerViewGroupState {
        this.retryFunc = func
        recyclerViewGroupWeakRef?.get()?.setRetryFunc(func)
        return this
    }

    fun setState(state: Int, errorReason: String? = null): RecyclerViewGroupState {
        this.state = state
        this.errorReason = errorReason
        updateState()
        return this
    }

    fun checkData(data: Collection<*>) {
        if (data.isEmpty()) empty() else successHasData()
    }

    fun empty() = setState(STATE_EMPTY)

    fun loading() = setState(STATE_LOADING)

    fun error(errorReason: String? = null) = setState(STATE_ERROR, errorReason)

    fun successHasData() = setState(STATE_SUCCESS_HAS_DATA)

    private fun updateState() {
        recyclerViewGroupWeakRef?.get()?.run {
            when (state) {
                STATE_EMPTY -> empty()
                STATE_LOADING -> loading()
                STATE_ERROR -> error(errorReason)
                STATE_SUCCESS_HAS_DATA -> successHasData()
            }
        }
    }
}

class RecyclerViewGroup @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var retryFunc: (() -> Unit)? = null

    init {
        LayoutInflater.from(context)
                .inflate(R.layout.view_recycler_view_group, this, true)
        if (attrs !== null) {
            val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.RecyclerViewGroup,
                    defStyleAttr, 0)
            try {
                emptyStateMessage.text = ta.getString(R.styleable.RecyclerViewGroup_emptyStateMessage)
                errorStateMessage.text = ta.getString(R.styleable.RecyclerViewGroup_errorStateMessage)
                val rvPaddingH = ta.getDimensionPixelOffset(
                        R.styleable.RecyclerViewGroup_rvPaddingHorizontal, 0)
                if (rvPaddingH > 0) {
                    recyclerView.setPadding(
                            rvPaddingH, 0,
                            rvPaddingH, 0
                    )
                }
            } finally {
                ta.recycle()
            }
        }

        errorStateRetryButton.setOnClickListener { retryFunc?.invoke() }
    }

    fun setEmptyStateMessage(text: CharSequence) {
        emptyStateMessage.text = text
    }

    fun setErrorStateMessage(text: CharSequence) {
        errorStateMessage.text = text
    }

    fun setRetryFunc(func: (() -> Unit)?) {
        retryFunc = func
        if (func === null) {
            errorStateRetryButton.gone()
        } else {
            errorStateRetryButton.visible()
        }
    }

    fun getRecyclerView(): RecyclerView = recyclerView

    private fun isRecyclerViewEmpty(): Boolean = (recyclerView.adapter?.itemCount ?: 0) == 0

    fun loading() {
        if (isRecyclerViewEmpty()) {
            progressBar.visible()
        } else {
            progressBar.gone()
        }
        emptyStateMessage.gone()
        errorStateGroup.gone()
    }

    fun empty() {
        progressBar.gone()
        emptyStateMessage.visible()
        errorStateGroup.gone()
    }

    fun error(reason: String? = null) {
        progressBar.gone()
        emptyStateMessage.gone()

        if (!isRecyclerViewEmpty()) {
            // show snack bar instead of errorStateGroup
            val snackBar = Snackbar.make(this, reason ?: "Error", Snackbar.LENGTH_LONG)
            retryFunc?.let { retry -> snackBar.setAction(R.string.retry, { retry.invoke() }) }
            snackBar.show()

            errorStateGroup.gone()
        } else {
            errorStateGroup.visible()
            if (reason !== null) {
                errorStateReason.visible()
                errorStateReason.text = reason
            } else {
                errorStateReason.gone()
            }
        }
    }

    fun successHasData() {
        progressBar.gone()
        emptyStateMessage.gone()
        errorStateGroup.gone()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // fix memory leak of RecyclerView
        recyclerView.adapter = null
    }

}
