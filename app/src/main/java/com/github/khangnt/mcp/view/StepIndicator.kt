package com.github.khangnt.mcp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.github.khangnt.mcp.R
import kotlin.math.ceil

/**
 * Created by Khang NT on 4/6/18.
 * Email: khang.neon.1997@gmail.com
 */

class StepIndicator @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var stepCount: Int
    private var step: Int

    private val paintStepPassed: Paint
    private val paintStepNormal: Paint

    private var indicatorColor: Int
    private var indicatorSize: Int
    private var indicatorSpace: Int


    init {
        val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.StepIndicator,
                0, R.style.StepIndicatorDefault)
        try {
            indicatorColor = ta.getColor(R.styleable.StepIndicator_indicator_color, 0)
            indicatorSize = ta.getDimensionPixelOffset(R.styleable.StepIndicator_indicator_size, 0)
            indicatorSpace = ta.getDimensionPixelOffset(R.styleable.StepIndicator_indicator_space, 0)
            stepCount = ta.getInt(R.styleable.StepIndicator_indicator_stepCount, 3)
            step = ta.getInt(R.styleable.StepIndicator_indicator_step, 1)
        } finally {
            ta.recycle()
        }

        check(indicatorSize > 0) { "Invalid indicator size $indicatorSize" }
        check(stepCount > 0) { "Invalid step count $stepCount" }
        check(step in 1..stepCount) { "Invalid step $step" }

        paintStepPassed = Paint(Paint.ANTI_ALIAS_FLAG)
        paintStepPassed.color = indicatorColor
        paintStepPassed.style = Paint.Style.FILL_AND_STROKE

        paintStepNormal = Paint(Paint.ANTI_ALIAS_FLAG)
        paintStepNormal.color = indicatorColor
        paintStepNormal.strokeWidth = context.resources.getDimension(R.dimen.dp) * 1.5f
        paintStepNormal.style = Paint.Style.STROKE
    }

    fun setStep(step: Int, stepCount: Int = this.stepCount) {
        check(stepCount > 0) { "Invalid step count $stepCount" }
        check(step in 1..stepCount) { "Invalid step $step" }
        this.step = step
        this.stepCount = stepCount
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = stepCount * indicatorSize + (stepCount - 1) * indicatorSpace +
                paintStepNormal.strokeWidth
        val height = indicatorSize + paintStepNormal.strokeWidth
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(ceil(width).toInt(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(ceil(height).toInt(), MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        val radius = indicatorSize / 2f
        val y = (indicatorSize + paintStepNormal.strokeWidth) / 2
        var x = y
        for (i in 1..stepCount) {
            canvas.drawCircle(x, y, radius, if (i <= step) paintStepPassed else paintStepNormal)
            x += indicatorSize + indicatorSpace
        }
    }

}