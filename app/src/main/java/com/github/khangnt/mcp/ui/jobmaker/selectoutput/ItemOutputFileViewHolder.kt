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

data class FileName(val name: String) : AdapterModel, HasIdLong {
    override val idLong: Long = IdGenerator.idFor(name)
}

class ItemOutputFileViewHolder(
        itemView: View
) : CustomViewHolder<FileName>(itemView) {

    private val ivEditName = itemView.ivEditName
    private val tvFileName = itemView.tvFileName
    private val tvFileId = itemView.tvFileId
    private var currentFile: String? = null

    init {
        ivEditName.setOnClickListener {
            // edit file name
        }
    }

    override fun bind(model: FileName, pos: Int) {
        currentFile = model.name
        val displayId = pos + 1

        model.name.apply {
            tvFileName.text = this
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