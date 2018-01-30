package com.github.khangnt.mcp.ui.filepicker

import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.CustomViewHolder
import com.github.khangnt.mcp.ui.common.HasIdModel
import kotlinx.android.synthetic.main.item_file_list.view.*
import java.io.File

/**
 * Created by Khang NT on 1/29/18.
 * Email: khang.neon.1997@gmail.com
 */

const val TYPE_FOLDER = 0
const val TYPE_FILE = 1
const val TYPE_CREATE_FOLDER = 2

data class FileListModel(
        val path: File,
        val type: Int,
        override val modelId: Long
) : AdapterModel, HasIdModel

class FileListViewHolder(
        itemView: View,
        onClickListener: (model: FileListModel, pos: Int) -> Unit,
        private val selectedIdRetriever: () -> Long?
) : CustomViewHolder<FileListModel>(itemView) {
    private val ivFileIcon = itemView.ivFileIcon
    private val tvFileName = itemView.tvFileName

    private var model: FileListModel? = null
    private var pos: Int? = null

    init {
        itemView.isSelected = true
        itemView.setOnClickListener { onClickListener(model!!, pos!!) }
    }

    override fun bind(model: FileListModel, pos: Int) {
        this.model = model
        this.pos = pos
        when(model.type) {
            TYPE_FOLDER -> {
                tvFileName.text = model.path.name
                setSelected(false)
                ivFileIcon.setImageResource(R.drawable.ic_folder_black_24dp)
            }
            TYPE_FILE -> {
                tvFileName.text = model.path.name
                ivFileIcon.setImageResource(R.drawable.ic_file_black_24dp)
                setSelected(selectedIdRetriever() == model.modelId)
            }
            TYPE_CREATE_FOLDER -> {
                tvFileName.setText(R.string.create_new_folder)
                setSelected(false)
                ivFileIcon.setImageResource(R.drawable.ic_create_new_folder_black_24dp)
            }
        }
    }

    private fun setSelected(selected: Boolean) {
        val drawable: Drawable? = if (selected) {
            ContextCompat.getDrawable(itemView.context, R.drawable.ic_tick_green_24dp)
        } else null

        tvFileName.isSelected = selected
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            tvFileName.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                    drawable, null)
        } else {
            tvFileName.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    drawable, null)
        }
    }
}