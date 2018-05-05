package com.github.khangnt.mcp.ui.jobmaker.selectfile

import android.view.MotionEvent
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

class ItemFileViewHolder(
        itemView: View,
        onStartDrag: (ItemFileViewHolder) -> Unit,
        onRemoveFile: (File) -> Unit
) : CustomViewHolder<FileModel>(itemView) {

    private val ivRemoveFile = itemView.ivRemove
    private val tvFileName = itemView.tvFileName
    private val tvFileId = itemView.tvFileId
    private val vDragHandle = itemView.vDragHandle

    private var currentFile: File? = null

    init {
        ivRemoveFile.setOnClickListener {
            onRemoveFile.invoke(checkNotNull(currentFile))
        }
        vDragHandle.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                onStartDrag(this)
            }
            return@setOnTouchListener false
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
        override val layoutRes: Int = R.layout.item_selected_file
        lateinit var onStartDrag: (ItemFileViewHolder) -> Unit
        lateinit var onRemoveFile: (File) -> Unit

        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemFileViewHolder(itemView, onStartDrag, onRemoveFile)
        }
    }

}