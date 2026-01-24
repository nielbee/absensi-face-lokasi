package com.example.absensi.ui.face.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class FaceOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = width.coerceAtMost(height) / 3f
        canvas.drawCircle(
            width / 2f,
            height / 2f,
            radius,
            paint
        )
    }
}
