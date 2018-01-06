package com.github.khangnt.mcp.view

import android.content.Context
import android.support.v7.widget.RecyclerView
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

    var recyclerView: RecyclerView? = null
    var emptyView: View? = null
    var errorView: View? = null
    var errorReason: TextView? = null
    var loadingView: View? = null

    var onRetry: (() -> Unit)? = null

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        when (child.id) {
            R.id.recyclerView -> recyclerView = child as RecyclerView
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
        loadingView?.visibility = VISIBLE
        emptyView?.visibility = GONE
        errorView?.visibility = GONE
    }

    fun empty() {
        loadingView?.visibility = GONE
        emptyView?.visibility = VISIBLE
        errorView?.visibility = GONE
    }

    fun error(reason: String? = null) {
        loadingView?.visibility = GONE
        emptyView?.visibility = GONE
        errorView?.visibility = VISIBLE
        errorReason?.let {
            it.visibility = if (reason != null) VISIBLE else GONE
            it.text = reason
        }
    }

    fun successWithData() {
        loadingView?.visibility = GONE
        emptyView?.visibility = GONE
        errorView?.visibility = GONE
    }

}