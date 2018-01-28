package com.github.khangnt.mcp.ui.common

import android.view.View
import android.widget.TextView
import com.github.khangnt.mcp.R

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */


open class HeaderModel(val header: String, idGenerator: IdGenerator): AdapterModel, HasIdModel {
    override val modelId: Long by lazy { idGenerator.idFor(header).toLong() }
}

open class ItemHeaderViewHolder(itemView: View) : CustomViewHolder<HeaderModel>(itemView) {
    val tvHeader by lazy { itemView.findViewById<TextView>(R.id.tvHeader)!! }

    override fun bind(model: HeaderModel, pos: Int) {
        tvHeader.text = model.header
    }
}