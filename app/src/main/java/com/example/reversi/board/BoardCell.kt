package com.example.reversi.board

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View


/**
 * TODO: document your custom view class.
 */
class BoardCell : View {
    var drawColor : Int = Color.TRANSPARENT
    private set

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        this.setBackgroundColor(Color.parseColor("#49c410"))

        if(drawColor != Color.TRANSPARENT){
            var paint = Paint()
            var drawLocation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20f, getContext().getResources().displayMetrics)
            paint.color = drawColor
            paint.setAntiAlias(true);

            var size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,39f, getContext().getResources().displayMetrics)
            canvas.drawCircle(drawLocation, drawLocation, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15f, getContext().getResources().displayMetrics), paint)
        }
    }

    fun setColor(color: Int){
        drawColor = color
    }

    fun changeColor(color: Int){
        drawColor = color
        invalidate()
    }
}
