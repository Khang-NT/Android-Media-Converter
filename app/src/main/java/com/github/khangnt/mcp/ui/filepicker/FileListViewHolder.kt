package com.github.khangnt.mcp.ui.filepicker

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
        onClickListener: (model: FileListModel) -> Unit
) : CustomViewHolder<FileListModel>(itemView) {
    private val ivFileIcon = itemView.ivFileIcon
    private val tvFileName = itemView.tvFileName

    private var model: FileListModel? = null

    init {
        itemView.setOnClickListener { model?.let(onClickListener::invoke) }
    }

    override fun bind(model: FileListModel, pos: Int) {
        this.model = model
        when(model.type) {
            TYPE_FOLDER -> {
                tvFileName.text = model.path.name
                ivFileIcon.setImageResource(R.drawable.ic_folder_black_24dp)
            }
            TYPE_FILE -> {
                tvFileName.text = model.path.name
                ivFileIcon.setImageResource(R.drawable.ic_file_black_24dp)
            }
            TYPE_CREATE_FOLDER -> {
                tvFileName.setText(R.string.create_new_folder)
                ivFileIcon.setImageResource(R.drawable.ic_create_new_folder_black_24dp)
            }
        }
    }
}