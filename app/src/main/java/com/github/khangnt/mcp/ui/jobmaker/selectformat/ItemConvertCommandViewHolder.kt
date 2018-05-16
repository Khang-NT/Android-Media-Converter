package com.github.khangnt.mcp.ui.jobmaker.selectformat

import android.graphics.drawable.GradientDrawable
import android.view.View
import com.github.khangnt.mcp.ConvertCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.disabledColors
import com.github.khangnt.mcp.ui.common.*
import kotlinx.android.synthetic.main.item_convert_command.view.*

/**
 * Created by Khang NT on 5/5/18.
 * Email: khang.neon.1997@gmail.com
 */

data class ConvertCommandModel(
        val command: ConvertCommand,
        val enabled: Boolean
) : AdapterModel, HasIdLong {
    override val idLong: Long = IdGenerator.idFor(command.name)
}

class ItemConvertCommandViewHolder(
        itemView: View,
        onClick: (ConvertCommandModel) -> Unit
) : CustomViewHolder<ConvertCommandModel>(itemView) {

    private val ivCardBackground = itemView.ivCardBackground
    private val tvShortName = itemView.tvShortName

    private var currentModel: ConvertCommandModel? = null

    init {
        itemView.setOnClickListener {
            onClick(checkNotNull(currentModel))
        }
    }

    override fun bind(model: ConvertCommandModel, pos: Int) {
        if (model != currentModel) {
            currentModel = model
            with(model.command) {
                val gradientColors = if (model.enabled) colors else disabledColors
                ivCardBackground.setImageDrawable(
                        GradientDrawable(GradientDrawable.Orientation.TL_BR, gradientColors))
                tvShortName.alpha = if (model.enabled) 1.0f else 0.5f
                tvShortName.setText(shortName)
            }
        }

    }

    class Factory(init: Factory.() -> Unit) : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_convert_command
        lateinit var onClick: (ConvertCommandModel) -> Unit

        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemConvertCommandViewHolder(itemView, onClick)
        }
    }

}