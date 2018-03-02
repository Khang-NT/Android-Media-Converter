package com.github.khangnt.mcp.ui.components

import android.content.Context
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import com.github.khangnt.mcp.R


/**
 * Created by Simon Pham on 3/2/18.
 * Email: simonpham.dn@gmail.com
 */

class ColoredButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (isSelected) {
            // update textColor, background from Widget.AppCompat.Button.Colored
            this.setTextColor(context.getColor(R.color.colorAccent))
            this.setBackgroundColor(context.getColor(R.color.backgroundColor))

        } else {
            // update textColor, background from Widget.AppCompat.Button.Borderless.Colored
            this.setTextColor(context.getColor(R.color.backgroundColor))
            this.setBackgroundColor(context.getColor(R.color.colorAccent))
        }
    }
}