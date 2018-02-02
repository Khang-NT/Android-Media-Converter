package com.github.khangnt.mcp.ui.filepicker

import android.view.View
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.CustomViewHolder
import com.github.khangnt.mcp.ui.common.HasIdString
import kotlinx.android.synthetic.main.item_path_indicator.view.*
import java.io.File

/**
 * Created by Khang NT on 1/31/18.
 * Email: khang.neon.1997@gmail.com
 */

data class PathIndicatorModel(
        val path: File
) : AdapterModel, HasIdString {
    override val idString: String = path.absolutePath
}

class PathIndicatorViewHolder(
        itemView: View,
        private val onClick: (model: PathIndicatorModel) -> Unit
) : CustomViewHolder<PathIndicatorModel>(itemView) {
    private val tvPathIndicator = itemView.tvPathIndicator
    private var model: PathIndicatorModel? = null

    init {
        itemView.setOnClickListener { onClick(model!!) }
    }

    override fun bind(model: PathIndicatorModel, pos: Int) {
        this.model = model
        tvPathIndicator.text = if (model.path.name.isNotEmpty()) model.path.name else "/"
    }
}