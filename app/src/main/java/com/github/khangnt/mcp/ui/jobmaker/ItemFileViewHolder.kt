package com.github.khangnt.mcp.ui.jobmaker

import android.view.View
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.CustomViewHolder
import com.github.khangnt.mcp.ui.common.ViewHolderFactory
import java.io.File

/**
 * Created by Simon Pham on 4/13/18.
 * Email: simonpham.dn@gmail.com
 */

data class FileModel(val file: File) : AdapterModel {
    val path: String = file.absolutePath
}

class ItemFileViewHolder(itemView: View) : CustomViewHolder<FileModel>(itemView) {
    override fun bind(model: FileModel, pos: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class Factory : ViewHolderFactory {
        override fun create(itemView: View): CustomViewHolder<*> = ItemFileViewHolder(itemView)

        override val layoutRes: Int = R.layout.item_selected_file


    }
}