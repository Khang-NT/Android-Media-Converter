package com.github.khangnt.mcp.ui.jobmaker.selectoutput

import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.*
import kotlinx.android.synthetic.main.item_output_files.view.*

/**
 * Created by Simon Pham on 5/8/18.
 * Email: simonpham.dn@gmail.com
 */

data class OutputFile(var fileName: String,
                      var fileExt: String,
                      var isConflict: Boolean = false,
                      var isOverrideAllowed: Boolean = false) : AdapterModel, HasIdLong {
    override val idLong: Long = IdGenerator.idFor(fileName)
}

class ItemOutputFileViewHolder(
        itemView: View,
        onEditFileNameClick: View.OnClickListener,
        onFileNameClick: View.OnClickListener
) : CustomViewHolder<OutputFile>(itemView) {

    private val context = itemView.context
    private val ivEditName = itemView.ivEditName
    private val tvFileName = itemView.tvFileName
    private val tvFileId = itemView.tvFileId

    init {
        ivEditName.setOnClickListener(onEditFileNameClick)
        tvFileName.setOnClickListener(onFileNameClick)
    }

    @SuppressLint("SetTextI18n")
    override fun bind(model: OutputFile, pos: Int) {
        val displayId = pos + 1

        model.apply {
            tvFileName.text = "$fileName.$fileExt"
            tvFileId.text = displayId.toString()
            ivEditName.tag = pos
            tvFileName.tag = pos
        }
        if (model.isConflict && !(model.isOverrideAllowed)) {
            tvFileName.isClickable = true
            tvFileName.error = "conflict"
            tvFileName.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        } else {
            tvFileName.isClickable = false
            tvFileName.error = null
            tvFileName.setTextColor(ContextCompat.getColor(context, android.R.color.secondary_text_light))
        }
    }

    class Factory(init: Factory.() -> Unit) : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_output_files
        lateinit var onEditFileNameClick: View.OnClickListener
        lateinit var onFileNameClick: View.OnClickListener

        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemOutputFileViewHolder(itemView, onEditFileNameClick, onFileNameClick)
        }
    }

}