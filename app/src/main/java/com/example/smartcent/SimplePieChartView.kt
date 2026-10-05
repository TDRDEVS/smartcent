package com.example.smartcent

import android.R.attr.width
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class SimplePieChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }
    private var slices: List<Triple<Int, Float, String>> = emptyList()
    private var minValue: Double = 0.0
    private var maxValue: Double = 0.0

    fun setData(data: Map<String, Double>, min: Double, max: Double) {
        val total = data.values.sum()
        minValue = min
        maxValue = max
        slices = data.entries.mapIndexed { index, entry ->
            val color = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW)[index % 4]
            val percent = (entry.value / total).toFloat()
            val label = "${entry.key}: R${"%.2f".format(entry.value)}"
            Triple(color, percent, label)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var startAngle = 0f
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val radius = width / 2f
        val cx = width / 2f
        val cy = height / 2f

        // Draw slices
        for ((color, percent, label) in slices) {
            paint.color = color
            val sweepAngle = percent * 360
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)

            // Label inside slice
            val angle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val labelX = (cx + (radius / 2) * Math.cos(angle)).toFloat()
            val labelY = (cy + (radius / 2) * Math.sin(angle)).toFloat()
            canvas.drawText(label, labelX, labelY, textPaint)

            startAngle += sweepAngle
        }

        // Reset text color for min/max labels
        textPaint.color = Color.BLACK
        textPaint.textSize = 32f
        canvas.drawText("Min: R${"%.2f".format(minValue)}", cx, height - 80f, textPaint)
        canvas.drawText("Max: R${"%.2f".format(maxValue)}", cx, height - 40f, textPaint)
    }
}
