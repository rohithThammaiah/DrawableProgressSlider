package com.rohith.progressselector

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class IndeterminateProgressBar : View {

    private var minValue: Float = 0f
    private var maxValue: Float = 1000f
    private var currentValue: Float = 0f

    var listeners: ProgressBarListeners? = null

    private var shouldDraw = false
    private var shouldAnimateDrawable = false

    private val valueAnimator = ValueAnimator()
    private var drawableAnimator = ValueAnimator()

    private var alphaValuesHolder = PropertyValuesHolder.ofInt("alpha",0,255)
    private var widthValuesHolder = PropertyValuesHolder.ofInt("drawableW",0,60)
    private var heightValuesHolder = PropertyValuesHolder.ofInt("drawableH",20,180)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }
    private val outerRectColor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36.0f
        textAlign = Paint.Align.CENTER

    }

    private val backgroundRect = Rect()
    private val progressRect = Rect()
    private val textRect = Rect()
    private val roundedRect1 = RectF()
    private val roundedRect2 = RectF()
    private val rectF = Rect()

    private var text = "0%"

    private val radiusHolder: PropertyValuesHolder = PropertyValuesHolder.ofFloat("radius", 150f, 5f)
    private val sizeHolder: PropertyValuesHolder = PropertyValuesHolder.ofFloat("size", 30f, 15f)

    private var radius = 5f
    private var size = 15f
    private var rotate = 0f

    private var drawableW = 24
    private var drawableH = 24

    private var progressBarStart:Int = 0
    private var progressBarHeight:Int = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val balloonDrawable: Drawable = resources.getDrawable(R.drawable.balloon_drawable, null)

    private var onActionDownPoint: Float = 0f
    private var dx = 0f

    private val assignedMargins = 40

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = 280 //calculated
        val width = measuredWidth //parent width

        setMeasuredDimension(width, height)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {

        progressBarStart = height - 35
        progressBarHeight = progressBarStart + 5

        text = "${currentValue.toInt()}"

        backgroundRect.left = assignedMargins
        backgroundRect.top = progressBarStart
        backgroundRect.right = width - assignedMargins
        backgroundRect.bottom =progressBarHeight

        progressRect.left = assignedMargins
        progressRect.top = progressBarStart
        progressRect.right = if (progressWidth(width - assignedMargins) >= assignedMargins) progressWidth(width-assignedMargins) else assignedMargins
        progressRect.bottom = progressBarHeight

        textPaint.getTextBounds(text, 0, text.length, textRect)

        canvas?.drawRect(backgroundRect, paint)
        canvas?.drawRect(progressRect, outerRectColor)

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


        paint.color = Color.WHITE
        canvas?.drawRoundRect(roundedRect1, radius, radius, outerRectColor)
        canvas?.drawRoundRect(roundedRect2, radius, radius, paint)

        canvas?.save()
        canvas?.rotate(rotate, progressRect.right.toFloat(), (progressRect.top - 30f))

        rectF.left = progressRect.right - drawableW
        rectF.right = progressRect.right + drawableW
        rectF.bottom = progressRect.top - 80
        rectF.top = progressRect.top - drawableH -20

        val layerList = balloonDrawable as? LayerDrawable

        layerList?.bounds = rectF

        Log.e("Animated","drawableW $drawableW drawableH $drawableH")

        Log.e("BalloonBounds", rectF.flattenToString())

        if (canvas != null)
            balloonDrawable.draw(canvas)

        canvas?.drawText(
            text,
            progressRect.right.toFloat(),
            progressRect.top.toFloat() - 125f,
            textPaint
        )
        canvas?.restore()

        super.onDraw(canvas)
    }

    private var userInputValue = 50f

    fun setProgress(value: Float) {

        if (value in minValue..maxValue) {
            if (value == minValue) {
                listeners?.onProgressStart(this)
            }

            Log.e("Value","$value")
            currentValue = value
            //userInputValue = value


            listeners?.onProgressChanged(this, this.currentValue)

            if (value == maxValue) {
                listeners?.onProgressCompleted(this)
            }
            shouldDraw = true
            invalidate()
        }else{
            //throw IllegalArgumentException("Value $value not between $minValue and $maxValue")
        }
    }

    private fun progressWidth(maxWidth: Int): Int {
        val calc = (currentValue * maxWidth) / maxValue
        Log.e("Calculation","($currentValue * $maxWidth)/$maxValue = $calc")
        return calc.toInt()
    }


    private fun animateBalloonOnTouchDown(){
        drawableAnimator.cancel()
        drawableAnimator.duration = 1000

        drawableAnimator.addUpdateListener {
            drawableW = it.getAnimatedValue("drawableW")as Int
            drawableH = it.getAnimatedValue("drawableH")as Int
            invalidate()
        }
        drawableAnimator.start()
    }

    private fun animateBalloonAngle(){
        valueAnimator.cancel()
        valueAnimator.duration = 800

        valueAnimator.addUpdateListener {
            radius = it.getAnimatedValue("radius") as Float
            size = it.getAnimatedValue("size") as Float
            invalidate()
        }

        valueAnimator.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {


        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {

                onActionDownPoint = event.x

                shouldDraw = false
                dx = ((event.rawX / backgroundRect.right))
                val progress = dx * maxValue
                Log.e("Modulo","$progress = $dx * $maxValue (${event.rawX} - ${backgroundRect.right})")
                setProgress(progress)

                val a = PropertyValuesHolder.ofFloat("radius",5f,150f)
                val b = PropertyValuesHolder.ofFloat("size",15f,30f)
                valueAnimator.setValues(a, b)
                drawableAnimator.setValues(alphaValuesHolder,widthValuesHolder,heightValuesHolder)

                animateBalloonAngle()
                animateBalloonOnTouchDown()

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                dx = (event.rawX / backgroundRect.right)

                val progress = dx * maxValue
                setProgress(progress)

                return true
            }

            MotionEvent.ACTION_UP -> {
                shouldDraw = false
                shouldAnimateDrawable = false
                valueAnimator.setValues(radiusHolder, sizeHolder)
                animateBalloonAngle()
                return true
            }

            else -> return false
        }
    }

    interface ProgressBarListeners {

        fun onProgressStart(view: View?)

        fun onProgressChanged(view: View?, value: Float)

        fun onProgressCompleted(view: View?)
    }

}