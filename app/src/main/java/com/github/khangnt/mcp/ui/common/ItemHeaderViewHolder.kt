package com.github.khangnt.mcp.ui.common

import android.view.View
import android.widget.TextView
import com.github.khangnt.mcp.R
import kotlinx.android.synthetic.main.item_header.view.*

/**
 * Created by Khang NT on 1/5/18.
 * Email: khang.neon.1997@gmail.com
 */


class HeaderModel(val header: String) : AdapterModel, HasIdLong {
    override val idLong: Long by lazy { IdGenerator.idFor(header) }
}

class ItemHeaderViewHolder(itemView: View) : CustomViewHolder<HeaderModel>(itemView) {
    private val tvHeader: TextView = itemView.tvHeader

    override fun bind(model: HeaderModel, pos: Int) {
        tvHeader.text = model.header
    }

    class Factory : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_header

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemHeaderViewHolder(itemView)
        }
    }

}