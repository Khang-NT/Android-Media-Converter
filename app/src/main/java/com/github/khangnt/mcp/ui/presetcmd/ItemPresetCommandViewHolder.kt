package com.github.khangnt.mcp.ui.presetcmd

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import com.github.khangnt.mcp.ConvertCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.AdapterModel
import com.github.khangnt.mcp.ui.common.CustomViewHolder
import com.github.khangnt.mcp.ui.common.HasIdLong
import com.github.khangnt.mcp.ui.common.ViewHolderFactory
import kotlinx.android.synthetic.main.item_preset_command.view.*

/**
 * Created by Khang NT on 2/5/18.
 * Email: khang.neon.1997@gmail.com
 */

data class PresetCommandModel(
        val presetCommand: ConvertCommand
) : AdapterModel, HasIdLong {
    override val idLong: Long = presetCommand.ordinal.toLong()
}

class ItemPresetCommandViewHolder(
        itemView: View,
        onClickListener: (ConvertCommand) -> Unit
) : CustomViewHolder<PresetCommandModel>(itemView) {
    private val ivIcon: ImageView = itemView.ivIcon
    private val tvShortName = itemView.tvShortName
//    private val tvTitle = itemView.tvTitle
//    private val tvDescription = itemView.tvDescription

    private var model: PresetCommandModel? = null

    init {
        itemView.setOnClickListener {
            // callback
            onClickListener.invoke(model!!.presetCommand)
        }
    }

    override fun bind(model: PresetCommandModel, pos: Int) {
        this.model = model
        with(model.presetCommand) {
            ivIcon.setImageDrawable(GradientDrawable(GradientDrawable.Orientation.TL_BR, colors))
            tvShortName.setText(shortName)
//            tvTitle.setText(titleRes)
//            tvDescription.setText(descriptionRes)
        }
    }

    class Factory(init: Factory.() -> Unit) : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_preset_command
        lateinit var onClickListener: (ConvertCommand) -> Unit

        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemPresetCommandViewHolder(itemView, onClickListener)
        }
    }

}