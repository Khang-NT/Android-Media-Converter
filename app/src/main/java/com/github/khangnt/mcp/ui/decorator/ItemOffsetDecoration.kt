package com.github.khangnt.mcp.ui.decorator

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Khang NT on 2/6/18.
 * Email: khang.neon.1997@gmail.com
 */

class ItemOffsetDecoration(
        val context: Context,
        var verticalSpace: Int = 0,
        var horizontalSpace: Int = 0
) : RecyclerView.ItemDecoration() {

    fun setVerticalSpace(offsetDimenRes: Int) = apply {
        verticalSpace = context.resources.getDimensionPixelOffset(offsetDimenRes)
    }

    fun setHorizontalSpace(offsetDimenRes: Int) = apply {
        horizontalSpace = context.resources.getDimensionPixelOffset(offsetDimenRes)
    }

    fun applyTo(parent: RecyclerView) = apply {
        val vPadding = verticalSpace / 2
        val hPadding = horizontalSpace / 2
        parent.setPadding(hPadding, vPadding, hPadding, vPadding)
        parent.clipToPadding = false

        parent.addItemDecoration(this)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val hOffset = horizontalSpace / 2
        val vOffset = verticalSpace / 2

        outRect.set(hOffset, vOffset, hOffset, vOffset)
    }

}