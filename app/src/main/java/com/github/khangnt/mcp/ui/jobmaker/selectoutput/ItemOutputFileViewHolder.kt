package com.github.khangnt.mcp.ui.jobmaker.selectoutput

import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.*
import kotlinx.android.synthetic.main.item_output_files.view.*
import java.io.File

/**
 * Created by Simon Pham on 5/8/18.
 * Email: simonpham.dn@gmail.com
 */

data class FileModel(val file: File) : AdapterModel, HasIdLong {
    override val idLong: Long = IdGenerator.idFor(file.absolutePath)
}

class ItemOutputFileViewHolder(
        itemView: View
) : CustomViewHolder<FileModel>(itemView) {

    private val ivEditName = itemView.ivEditName
    private val tvFileName = itemView.tvFileName
    private val tvFileId = itemView.tvFileId
    private var currentFile: File? = null

    init {
        ivEditName.setOnClickListener {
            // edit file name
        }
    }

    override fun bind(model: FileModel, pos: Int) {
        currentFile = model.file
        val displayId = pos + 1

        model.file.apply {
            tvFileName.text = this.name
            tvFileId.text = displayId.toString()
        }
    }

    class Factory(init: Factory.() -> Unit) : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_output_files

        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemOutputFileViewHolder(itemView)
        }
    }

}