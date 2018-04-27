package com.github.khangnt.mcp.ui.jobmaker

import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.CustomViewHolder
import com.github.khangnt.mcp.ui.common.ViewHolderFactory
import com.github.khangnt.mcp.util.getViewModel
import kotlinx.android.synthetic.main.item_selected_file.view.*
import java.io.File

/**
 * Created by Simon Pham on 4/13/18.
 * Email: simonpham.dn@gmail.com
 */

data class FileModel(val file: File) : AdapterModel {
    val path: String = file.absolutePath
}

class ItemFileViewHolder(itemView: View) : CustomViewHolder<FileModel>(itemView) {

    class Factory : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_selected_file
        override fun create(itemView: View): CustomViewHolder<*> = ItemFileViewHolder(itemView)
    }

    private val context = itemView.context
    private val ivRemoveFile = itemView.ivRemove
    private val tvFileName = itemView.tvFileName
    private val tvFileId = itemView.tvFileId

    private var currentFile: File? = null

    init {
        ivRemoveFile.setOnClickListener {
            removeFile(currentFile!!)
        }

        tvFileName.setOnClickListener {
            // do something
        }

        tvFileId.setOnClickListener {

        }
    }

    override fun bind(model: FileModel, pos: Int) {
        currentFile = model.file

        model.file.apply {
            tvFileName.text = model.path
            tvFileId.text = pos.toString()
        }
    }

    private fun removeFile(file: File) {
        // deselect file

    }
}