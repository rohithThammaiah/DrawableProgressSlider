package com.rohith.progressselector

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.toPointF
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator


class IndeterminateProgressBar : View {

    var maxValue: Int = 100
    var minValue: Int = 0
    var currentValue: Int = 0
    var listeners: ProgressBarListeners? = null
    var secondRectWidth: Int = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thirdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = resources.getColor(R.color.colorPrimary)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20.0f
        textAlign = Paint.Align.CENTER
    }
    private val backgroundRect = Rect()
    private val progressRect = Rect()
    private val textRect = Rect()
    private val line = Rect()

    val a = Point()
    val b = Point()
    val c = Point()

    var text = "0%"

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = 250 //calculated
        val width = measuredWidth //parent width

        setMeasuredDimension(width, height)
    }

    val path = Path()

    val roundedRect1 = RectF()
    val roundedRect2 = RectF()

    override fun onDraw(canvas: Canvas?) {
        text = "$currentValue"
        paint.color = ContextCompat.getColor(context, R.color.colorAccent)

        backgroundRect.top = height / 2
        backgroundRect.left = 25
        backgroundRect.bottom = height / 2 + 10
        backgroundRect.right = width - 25

        progressRect.top = height / 2
        progressRect.left = 25
        progressRect.bottom = height / 2 + 10
        progressRect.right = if (progressWidth(width) - 25 > 25) progressWidth(width) - 25 else ++backgroundRect.left

        line.top = height / 2 - 35
        line.left = progressRect.right - 1
        line.right = progressRect.right + 1
        line.bottom = progressRect.top

        if (currentValue == maxValue) {
            secondPaint.color = ContextCompat.getColor(context, R.color.colorPrimary)
            thirdPaint.color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        } else {
            secondPaint.color = resources.getColor(R.color.colorPrimary)
        }

        textPaint.getTextBounds(text, 0, text.length, textRect)

        if (BuildConfig.DEBUG) {
            Log.e("ProgressBar", "currentValue : $currentValue")
            Log.e("ProgressBar", "backgroundRect : $backgroundRect")
            Log.e("ProgressBar", "progressRect: $progressRect")
            Log.e("ProgressBar", "line: $line")
        }

        val circleY = progressRect.top + (progressRect.bottom - progressRect.top) / 2f



        canvas?.drawRect(backgroundRect, paint)
        canvas?.drawRect(progressRect, secondPaint)



        /*canvas?.drawCircle(progressRect.right.toFloat(), circleY, 27f, secondPaint)
        paint.color = Color.WHITE
        canvas?.drawCircle(progressRect.right.toFloat(), circleY, 23f, paint)*/

        val paint1 = Paint()

        paint1.strokeWidth = 1.5f
        paint1.color = resources.getColor(R.color.colorPrimaryDark)

        paint1.style = Paint.Style.FILL_AND_STROKE
        paint1.isAntiAlias = true
        if (shouldDraw) {

            canvas?.drawCircle(progressRect.right.toFloat(), circleY, 27f, secondPaint)
            paint.color = Color.WHITE
            canvas?.drawCircle(progressRect.right.toFloat(), circleY, 23f, paint)

            a.set(progressRect.right - 34, progressRect.top / 2 - 25)
            b.set(progressRect.right + 34, progressRect.top / 2 - 25)
            c.set(progressRect.right, (progressRect.top - 25))

            Log.e("Triangle", "$a\n$b\n$c")

            path.reset()

            path.fillType = Path.FillType.EVEN_ODD
            path.moveTo(a.x.toFloat(), a.y.toFloat())
            path.lineTo(b.x.toFloat(), b.y.toFloat())
            //path.moveTo(b.x.toFloat(), b.y.toFloat())
            path.lineTo(c.x.toFloat(), c.y.toFloat())
            //path.moveTo(c.x.toFloat(), c.y.toFloat())
            path.lineTo(a.x.toFloat(), a.y.toFloat())
            path.close()

            canvas?.drawPath(path, paint1)
            canvas?.drawCircle(progressRect.right.toFloat(), progressRect.top.toFloat() / 2 - 23, 35.0f, thirdPaint)
            canvas?.drawText(
                text,
                progressRect.right.toFloat(),
                (progressRect.top.toFloat() + 10.0f) / 2.0f - 23,
                textPaint
            )
            // canvas?.drawRect(line,thirdPaint)
        }else{
            /*val propertyRadius = PropertyValuesHolder.ofInt("", 0, 150)
            val propertyRotate = PropertyValuesHolder.ofInt(PROPERTY_ROTATE, 0, 360)*/


            roundedRect1.left = progressRect.right - 20f
            roundedRect1.top = progressRect.top -15f
            roundedRect1.right = progressRect.right + 20f
            roundedRect1.bottom = progressRect.bottom + 15f

            roundedRect2.left = progressRect.right - 7.5f
            roundedRect2.top = progressRect.top -2.5f
            roundedRect2.right = progressRect.right + 7.5f
            roundedRect2.bottom = progressRect.bottom + 2.5f

            paint.color = Color.WHITE

            canvas?.drawRoundRect(roundedRect1,150f,150f,thirdPaint)
            canvas?.drawRoundRect(roundedRect2,150f,150f,paint)
        }

        /*val valueAnimator = ValueAnimator.ofFloat(5f,150f)

        valueAnimator.duration = 1000

        valueAnimator.addUpdateListener {
            val radius =
        }*/


        super.onDraw(canvas)
    }


    var shouldDraw = false


    fun setProgress(value: Int) {

        if (value in minValue..maxValue) {
            if (value == minValue) {
                listeners?.onProgressStart(this)
            }
            this.currentValue = value
            listeners?.onProgressChanged(this, this.currentValue)

            if (value == maxValue) {
                listeners?.onProgressCompleted(this)
            }

            invalidate()
        }
    }

    private fun progressWidth(maxWidth: Int): Int {
        secondRectWidth = (currentValue * maxWidth) / maxValue


        return secondRectWidth
    }


    interface ProgressBarListeners {

        fun onProgressStart(view: View?)

        fun onProgressChanged(view: View?, value: Int)

        fun onProgressCompleted(view: View?)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        var dx = 0f

        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {

                shouldDraw = true
                dx = ((event?.rawX / backgroundRect.right) * 100f)

                setProgress(dx.toInt())

                Log.e("TouchEventDown", "RawX -> ${event?.rawX} ${backgroundRect.right} $dx")
                //invalidate()

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                dx = (event?.rawX / backgroundRect.right) * 100f

                setProgress(dx.toInt())
                invalidate()

                return true
            }

            MotionEvent.ACTION_UP -> {
                shouldDraw = false
                invalidate()
                return true
            }

            else -> return false
        }
    }

}