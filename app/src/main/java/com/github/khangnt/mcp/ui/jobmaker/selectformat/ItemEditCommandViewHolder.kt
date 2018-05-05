package com.github.khangnt.mcp.ui.jobmaker.selectformat

import android.graphics.drawable.GradientDrawable
import android.view.View
import com.github.khangnt.mcp.EditCommand
import com.github.khangnt.mcp.R
import com.github.khangnt.mcp.disabledColors
import com.github.khangnt.mcp.ui.common.*
import kotlinx.android.synthetic.main.item_edit_command.view.*

/**
 * Created by Khang NT on 5/5/18.
 * Email: khang.neon.1997@gmail.com
 */

data class EditCommandModel(
        val editCommand: EditCommand,
        val enabled: Boolean
) : AdapterModel, HasIdLong {
    override val idLong: Long = IdGenerator.idFor(editCommand.name)
}

class ItemEditCommandViewHolder(
        itemView: View,
        onClick: (EditCommandModel) -> Unit
) : CustomViewHolder<EditCommandModel>(itemView) {

    private val ivCardBackground = itemView.ivCardBackground
    private val ivIcon = itemView.ivIcon
    private val tvLabel = itemView.tvLabel

    private var currentModel: EditCommandModel? = null

    init {
        itemView.setOnClickListener {
            onClick(checkNotNull(currentModel))
        }
    }

    override fun bind(model: EditCommandModel, pos: Int) {
        if (currentModel != model) {
            currentModel = model
            with(model.editCommand) {
                val gradientColors = if (model.enabled) colors else disabledColors
                ivCardBackground.setImageDrawable(
                        GradientDrawable(GradientDrawable.Orientation.TL_BR, gradientColors))
                ivIcon.setImageResource(iconRes)
                tvLabel.setText(label)
            }
        }
    }

    class Factory(init: Factory.() -> Unit) : ViewHolderFactory {
        override val layoutRes: Int = R.layout.item_edit_command
        lateinit var onClick: (EditCommandModel) -> Unit

        init {
            init()
        }

        override fun create(itemView: View): CustomViewHolder<*> {
            return ItemEditCommandViewHolder(itemView, onClick)
        }
    }

}