package com.github.khangnt.mcp.ui.jobmaker.selectoutput

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.*
import kotlinx.android.synthetic.main.item_output_files.view.*

/**
 * Created by Simon Pham on 5/8/18.
 * Email: simonpham.dn@gmail.com
 */

data class OutputFileAdapterModel(
        val fileName: String,
        val isConflict: Boolean = false,
        val isOverrideAllowed: Boolean = false
) : AdapterModel, HasIdLong {
    override val idLong: Long = IdGenerator.idFor(fileName)
}

class ItemOutputFileViewHolder(
        itemView: View,
        onEdit: (model: OutputFileAdapterModel, position: Int) -> Unit,
        onResolveConflict: (model: OutputFileAdapterModel, position: Int) -> Unit
) : CustomViewHolder<OutputFileAdapterModel>(itemView) {

    private val context = itemView.context
    private val ivEditName = itemView.ivEditName
    private val tvFileName = itemView.tvFileName
    private val tvFileId = itemView.tvFileId
    private val textColorNormal = tvFileName.currentTextColor

    private var currentModel: OutputFileAdapterModel? = null

    init {
        itemView.setOnClickListener {
            val adapterPos = adapterPosition
            if (adapterPos != RecyclerView.NO_POSITION) {
                // only clickable when isConflict == true
                check(currentModel?.isConflict == true)
                onResolveConflict(checkNotNull(currentModel), adapterPosition)
            }
        }

        ivEditName.setOnClickListener {
            val adapterPos = adapterPosition
            if (adapterPos != RecyclerView.NO_POSITION) {
                onEdit(checkNotNull(currentModel), adapterPosition)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bind(model: OutputFileAdapterModel, pos: Int) {
        this.currentModel = model
        val displayId = pos + 1

        model.apply {
            tvFileName.text = fileName
            tvFileId.text = displayId.toString()
            if (isConflict && !isOverrideAllowed) {
                itemView.isClickable = true
                tvFileName.error = context.getString(R.string.file_exists)
                tvFileName.setTextColor(Color.RED)
            } else {
                itemView.isClickable = false
                tvFileName.error = null
                tvFileName.setTextColor(textColorNormal)
            }
        }
    }

    class Factory(init: Factory.() -> Unit) : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_output_files
        lateinit var onEdit: (model: OutputFileAdapterModel, position: Int) -> Unit
        lateinit var onResolveConflict: (model: OutputFileAdapterModel, position: Int) -> Unit

        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemOutputFileViewHolder(itemView, onEdit, onResolveConflict)
        }
    }

}