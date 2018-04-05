package com.github.khangnt.mcp.ui.filepicker

import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.*
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
        var selected: Boolean = false
) : AdapterModel, HasIdLong {
    override val idLong: Long by lazy { IdGenerator.idFor(path.toString()) }
}

class FileListViewHolder(
        itemView: View,
        onClickListener: (model: FileListModel, pos: Int) -> Unit
) : CustomViewHolder<FileListModel>(itemView) {
    private val ivFileIcon = itemView.ivFileIcon
    private val tvFileName = itemView.tvFileName

    private var model: FileListModel? = null
    private var pos: Int? = null

    init {
        itemView.isSelected = true
        itemView.setOnClickListener {
            onClickListener(model!!, pos!!)
            setSelected(model!!.selected)
        }
    }

    override fun bind(model: FileListModel, pos: Int) {
        if (model != this.model || pos != this.pos) {
            this.model = model
            this.pos = pos
            when (model.type) {
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

        // check selected state
        setSelected(model.selected)
    }

    private fun setSelected(selected: Boolean) {
        if (selected != tvFileName.isSelected) {
            tvFileName.isSelected = selected
            val drawable: Drawable? = if (selected) {
                ContextCompat.getDrawable(itemView.context, R.drawable.ic_tick_green_24dp)
            } else null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tvFileName.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                        drawable, null)
            } else {
                tvFileName.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        drawable, null)
            }
        }
    }

    class Factory(init: Factory.() -> Unit): ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_file_list
        lateinit var onClickListener: (model: FileListModel, pos: Int) -> Unit
        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return FileListViewHolder(itemView, onClickListener)
        }
    }
}