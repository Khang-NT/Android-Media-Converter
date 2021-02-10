package com.github.khangnt.mcp.ui.jobmaker.selectformat

import android.graphics.drawable.GradientDrawable
import android.view.View
import com.github.khangnt.mcp.AdvancedCommand
import com.github.khangnt.mcp.Gradient
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.ui.common.*
import kotlinx.android.synthetic.main.item_convert_command.view.*

/**
 * Created by Khang NT on 5/5/18.
 * Email: khang.neon.1997@gmail.com
 */

data class AdvancedCommandModel(
        val command: AdvancedCommand,
        val enabled: Boolean
) : AdapterModel, HasIdLong {
    override val idLong: Long = IdGenerator.idFor(command.name)
}

class ItemAdvancedCommandViewHolder(
        itemView: View,
        onClick: (AdvancedCommandModel) -> Unit
) : CustomViewHolder<AdvancedCommandModel>(itemView) {

    private val ivCardBackground = itemView.ivCardBackground
    private val tvShortName = itemView.tvShortName

    private var currentModel: AdvancedCommandModel? = null

    init {
        itemView.setOnClickListener {
            onClick(checkNotNull(currentModel))
        }
    }

    override fun bind(model: AdvancedCommandModel, pos: Int) {
        if (model != currentModel) {
            currentModel = model
            with(model.command) {
                val gradientColor = if (model.enabled) gradient else Gradient.Disabled
                ivCardBackground.setImageDrawable(
                        gradientColor.getDrawable(GradientDrawable.Orientation.TL_BR))
                tvShortName.alpha = if (model.enabled) 1.0f else 0.5f
                tvShortName.setText(shortName)
            }
        }

    }

    class Factory(init: Factory.() -> Unit) : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_convert_command
        lateinit var onClick: (AdvancedCommandModel) -> Unit

        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemAdvancedCommandViewHolder(itemView, onClick)
        }
    }

}