package com.github.khangnt.mcp.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.khangnt.mcp.R

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */


open class HeaderModel(val header: String, idGenerator: IdGenerator): AdapterModel, HasIdModel {
    override val modelId: Long by lazy { idGenerator.idFor(header).toLong() }
}

open class ItemHeaderViewHolder<in T : HeaderModel>(itemView: View) : CustomViewHolder<T>(itemView) {
    val tvHeader by lazy { itemView.findViewById<TextView>(R.id.tvHeader)!! }

    override fun bind(model: T, pos: Int) {
        tvHeader.text = model.header
    }

    object Factory : ViewHolderFactory {
        override fun invoke(inflater: LayoutInflater, parent: ViewGroup): CustomViewHolder<*> =
            ItemHeaderViewHolder<HeaderModel>(inflater.inflate(R.layout.item_header, parent, false))
    }
}