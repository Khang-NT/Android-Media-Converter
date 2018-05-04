package com.github.khangnt.mcp.ui.jobmaker.selectfile

import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.*
import kotlinx.android.synthetic.main.item_selected_file.view.*
import java.io.File

/**
 * Created by Simon Pham on 4/13/18.
 * Email: simonpham.dn@gmail.com
 */

data class FileModel(val file: File) : AdapterModel, HasIdLong {
    override val idLong: Long = IdGenerator.idFor(file.absolutePath)
}

class ItemFileViewHolder(itemView: View) : CustomViewHolder<FileModel>(itemView) {

    class Factory : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_selected_file
        override fun create(itemView: View): CustomViewHolder<*> = ItemFileViewHolder(itemView)
    }

    private val ivRemoveFile = itemView.ivRemove
    private val tvFileName = itemView.tvFileName
    private val tvFileId = itemView.tvFileId

    private var currentFile: File? = null

    init {
//        ivRemoveFile.setOnClickListener {
//            removeFile(currentFile!!)
//        }
//
//        tvFileName.setOnClickListener {
//            // do something
//        }
//
//        tvFileId.setOnClickListener {
//
//        }
    }

    override fun bind(model: FileModel, pos: Int) {
        currentFile = model.file
        val displayId = pos + 1

        model.file.apply {
            tvFileName.text = this.name
            tvFileId.text = displayId.toString()
        }
    }

    private fun removeFile(file: File) {
        // deselect file

    }
}