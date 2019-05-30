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
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.OvershootInterpolator


class IndeterminateProgressBar : View {

    var maxValue: Int = 100
    var minValue: Int = 0
    var currentValue: Int = 0
    var listeners: ProgressBarListeners? = null
    var secondRectWidth: Int = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thirdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 20.0f
        textAlign = Paint.Align.CENTER
    }

    private val backgroundRect = Rect()
    private val progressRect = Rect()
    private val textRect = Rect()

    val a = Point()
    val b = Point()
    val c = Point()

    var text = "0%"

    private val radiusHolder: PropertyValuesHolder = PropertyValuesHolder.ofFloat("radius", 150f, 5f)
    private val sizeHolder: PropertyValuesHolder = PropertyValuesHolder.ofFloat("size", 30f, 15f)

    private val path = Path()

    private val roundedRect1 = RectF()
    private val roundedRect2 = RectF()

    private val valueAnimator = ValueAnimator()

    private var radius = 5f
    var size = 15f
    var rotate = 0f

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

    override fun onDraw(canvas: Canvas?) {
        text = "$currentValue"
        paint.color = ContextCompat.getColor(context, R.color.colorAccent)

        backgroundRect.top = height / 2
        backgroundRect.left = 40
        backgroundRect.bottom = height / 2 + 5
        backgroundRect.right = width - 40

        progressRect.top = height / 2
        progressRect.left = 40
        progressRect.bottom = height / 2 + 5
        progressRect.right = if (progressWidth(width) - 40 > 40) progressWidth(width) - 40 else ++backgroundRect.left

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
        }

        val circleY = progressRect.top + (progressRect.bottom - progressRect.top) / 2f



        canvas?.drawRect(backgroundRect, paint)
        canvas?.drawRect(progressRect, secondPaint)

        val paint1 = Paint()

        paint1.strokeWidth = 1.5f
        paint1.color = resources.getColor(R.color.colorPrimaryDark)

        paint1.style = Paint.Style.FILL_AND_STROKE
        paint1.isAntiAlias = true

        val progressMiddle = (progressRect.bottom - progressRect.top) / 2

        roundedRect1.left = progressRect.right - size
        roundedRect1.right = progressRect.right + size
        roundedRect1.top = progressRect.top - size + progressMiddle
        roundedRect1.bottom = progressRect.bottom + size - progressMiddle

        var sizeInside = size / 2
        if (shouldDraw)
            sizeInside = size - 3f

        roundedRect2.left = progressRect.right - sizeInside
        roundedRect2.right = progressRect.right + sizeInside
        roundedRect2.top = progressRect.top - sizeInside + progressMiddle
        roundedRect2.bottom = progressRect.bottom + sizeInside - progressMiddle


        canvas?.drawRoundRect(roundedRect1, radius, radius, thirdPaint)
        paint.color = Color.WHITE
        canvas?.drawRoundRect(roundedRect2, radius, radius, paint)

        a.set(progressRect.right - 33, progressRect.top / 2 - 30)
        b.set(progressRect.right + 33, progressRect.top / 2 - 30)
        c.set(progressRect.right, (progressRect.top - 30))

        Log.e("Triangle", "$a\n$b\n$c")

        path.reset()

        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(a.x.toFloat(), a.y.toFloat())
        path.lineTo(b.x.toFloat(), b.y.toFloat())
        path.lineTo(c.x.toFloat(), c.y.toFloat())
        path.lineTo(a.x.toFloat(), a.y.toFloat())
        path.close()
        paint.color = Color.WHITE

        canvas?.save()
        canvas?.rotate(rotate, c.x.toFloat(), c.y.toFloat())
        Log.e("rotate", "$rotate")
        canvas?.drawPath(path, paint1)
        canvas?.drawCircle(progressRect.right.toFloat(), progressRect.top.toFloat() / 2 - 23, 35.0f, thirdPaint)
        canvas?.drawText(
            text,
            progressRect.right.toFloat(),
            (progressRect.top.toFloat() + 10.0f) / 2.0f - 23,
            textPaint
        )
        canvas?.restore()

        if (shouldDraw) {

            valueAnimator.cancel()

            valueAnimator.setValues(radiusHolder, sizeHolder)

            valueAnimator.duration = 1000

            valueAnimator.addUpdateListener {
                //rotate = it.getAnimatedValue("angle") as Float
                radius = it.getAnimatedValue("radius") as Float
                size = it.getAnimatedValue("size") as Float
                invalidate()
            }
            valueAnimator.start()
        }




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
            shouldDraw = true
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

    private var onActionDownPoint: Float = 0f
    var dx = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {


        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {

                onActionDownPoint = event.x

                shouldDraw = true
                dx = ((event?.rawX / backgroundRect.right) * 100f)

                setProgress(dx.toInt())

                // rotate = -30f
                Log.e("TouchEventDown", "RawX -> ${event?.rawX} ${backgroundRect.right} $dx")
                //invalidate()

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                dx = (event?.rawX / backgroundRect.right) * 100f

                setProgress(dx.toInt())

                rotate = -30f

                if (event.x < onActionDownPoint) {
                    rotate = 30f
                }

                /*var valueAnimator = ValueAnimator.ofFloat(-30f, 0f)

                Log.e("Differece", "${onActionDownPoint}  ${event.x}")

                if (event.x < onActionDownPoint) {
                    valueAnimator = ValueAnimator.ofFloat(30f, 0f)
                }



                valueAnimator.addUpdateListener {
                    rotate = it.animatedValue as Float
                }

                valueAnimator.interpolator = OvershootInterpolator()
                valueAnimator.startDelay = 100

                valueAnimator.start()*/

                invalidate()

                return true
            }

            MotionEvent.ACTION_UP -> {
                shouldDraw = false

                rotate = 0f
                invalidate()
                return true
            }

            else -> return false
        }
    }

}