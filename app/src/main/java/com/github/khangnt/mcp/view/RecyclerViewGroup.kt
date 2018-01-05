package com.github.khangnt.mcp.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.github.khangnt.mcp.R

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */

class RecyclerViewGroup @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var recyclerView: View? = null
    private var emptyView: View? = null
    private var errorView: View? = null
    private var errorReason: TextView? = null
    private var loadingView: View? = null

    var onRetry: (() -> Unit)? = null

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        when (child.id) {
            R.id.recyclerView -> recyclerView = child
            R.id.loadingState -> loadingView = child
            R.id.emptyState -> emptyView = child
            R.id.errorState -> {
                errorView = child
                errorReason = child.findViewById(R.id.errorStateReasonTextView)
                (child.findViewById(R.id.errorStateRetryView) ?: child).setOnClickListener { onRetryClick() }
            }
        }
    }

    private fun onRetryClick() {
        onRetry?.invoke()
    }

    fun loading() {
        recyclerView?.visibility = INVISIBLE
        loadingView?.visibility = VISIBLE
        emptyView?.visibility = GONE
        errorView?.visibility = GONE
    }

    fun empty() {
        recyclerView?.visibility = INVISIBLE
        loadingView?.visibility = GONE
        emptyView?.visibility = VISIBLE
        errorView?.visibility = GONE
    }

    fun error(reason: String? = null) {
        recyclerView?.visibility = INVISIBLE
        loadingView?.visibility = GONE
        emptyView?.visibility = GONE
        errorView?.visibility = VISIBLE
        errorReason?.let {
            it.visibility = if (reason != null) VISIBLE else GONE
            it.text = reason
        }
    }

    fun successWithData() {
        recyclerView?.visibility = VISIBLE
        loadingView?.visibility = GONE
        emptyView?.visibility = GONE
        errorView?.visibility = GONE
    }

}