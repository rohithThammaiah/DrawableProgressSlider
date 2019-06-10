package com.rohith.progressselector

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat

class IndeterminateProgressBar : View {

    var maxValue: Int = 100
    var minValue: Int = 0
    var currentValue: Int = 0

    var listeners: ProgressBarListeners? = null

    var shouldDraw = false
    var shouldAnimateDrawable = false

    var secondRectWidth: Int = 0
    var drawableW = 80

    var drawableH = 220
    var alphaD = 0

    var drawableAnimator = ValueAnimator()
    private var alphaValuesHolder = PropertyValuesHolder.ofInt("alpha",0,255)
    private var widthValuesHolder = PropertyValuesHolder.ofInt("drawableW",0,80)
    private var heightValuesHolder = PropertyValuesHolder.ofInt("drawableH",0,220)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint1 = Paint()
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thirdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40.0f
        textAlign = Paint.Align.CENTER
    }
    private val backgroundRect = Rect()
    private val progressRect = Rect()
    private val textRect = Rect()

    var text = "0%"
    private val radiusHolder: PropertyValuesHolder = PropertyValuesHolder.ofFloat("radius", 150f, 5f)

    private val sizeHolder: PropertyValuesHolder = PropertyValuesHolder.ofFloat("size", 30f, 15f)
    private val roundedRect1 = RectF()

    private val roundedRect2 = RectF()
    private val valueAnimator = ValueAnimator()

    private val valueAnimatorRev = ValueAnimator()
    private var radius = 5f
    var size = 15f
    var rotate = 0f
    private val rectF = Rect()

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

        val height = 280 //calculated
        val width = measuredWidth //parent width

        setMeasuredDimension(width, height)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {

        text = "00:$currentValue"
        paint.color = ContextCompat.getColor(context, R.color.colorAccent)

        backgroundRect.top = height - 35
        backgroundRect.left = 40
        backgroundRect.bottom = height -35 + 5
        backgroundRect.right = width - 40

        progressRect.top = height - 35
        progressRect.left = 40
        progressRect.bottom = height -35 + 5
        progressRect.right = if (progressWidth(width) - 40 > 40) progressWidth(width) - 40 else ++backgroundRect.left

        textPaint.getTextBounds(text, 0, text.length, textRect)

        if (BuildConfig.DEBUG) {
            Log.e("ProgressBar", "currentValue : $currentValue")
            Log.e("ProgressBar", "backgroundRect : $backgroundRect")
            Log.e("ProgressBar", "progressRect: $progressRect")
        }

        canvas?.drawRect(backgroundRect, paint)
        canvas?.drawRect(progressRect, secondPaint)

        paint1.strokeWidth = 1.5f
        paint1.color = ContextCompat.getColor(context,R.color.colorPrimaryDark)

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


        paint.color = Color.WHITE
        canvas?.drawRoundRect(roundedRect1, radius, radius, thirdPaint)
        canvas?.drawRoundRect(roundedRect2, radius, radius, paint)

        canvas?.save()
        canvas?.rotate(rotate, progressRect.right.toFloat(), (progressRect.top - 30f))
        Log.e("rotate", "$rotate")

        val d = resources.getDrawable(R.drawable.balloon_drawable, null)

        rectF.left = progressRect.right - drawableW
        rectF.top = progressRect.top - drawableH
        rectF.right = progressRect.right + drawableW
        rectF.bottom = progressRect.top - 60

        d.bounds = rectF

        d.alpha = alphaD

        Log.e("BalloonBounds", rectF.flattenToString())

        d.draw(canvas)

        canvas?.drawText(
            text,
            progressRect.right.toFloat(),
            progressRect.top.toFloat() - 150f,
            textPaint
        )
        canvas?.restore()

        if (currentValue == maxValue) {
            secondPaint.color = ContextCompat.getColor(context, R.color.colorPrimary)
            thirdPaint.color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        } else {
            secondPaint.color = ContextCompat.getColor(context,R.color.colorPrimary)
        }

        super.onDraw(canvas)
    }

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
           // shouldAnimateDrawable = true
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

    private fun animateBalloonOnTouchDown(){
        drawableAnimator.cancel()
        drawableAnimator.duration = 1000

        drawableAnimator.addUpdateListener {

            alphaD = it.getAnimatedValue("alpha") as Int
            drawableW = it.getAnimatedValue("drawableW")as Int
            drawableH = it.getAnimatedValue("drawableH")as Int
            invalidate()
        }
        drawableAnimator.start()
    }

    private fun animateBalloonAngle(){
        valueAnimator.cancel()
        valueAnimator.duration = 1000

        valueAnimator.addUpdateListener {
            rotate = it.getAnimatedValue("angle") as Float
            radius = it.getAnimatedValue("radius") as Float
            size = it.getAnimatedValue("size") as Float
            invalidate()
        }

        valueAnimator.start()
    }

    private var onActionDownPoint: Float = 0f
    var dx = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {


        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {

                onActionDownPoint = event.x

                shouldDraw = false
                //shouldAnimateDrawable = true
                dx = ((event.rawX / backgroundRect.right) * 100f)

                setProgress(dx.toInt())
                Log.e("TouchEventDown", "RawX -> ${event?.rawX} ${backgroundRect.right} $dx")


                drawableAnimator.setValues(alphaValuesHolder,widthValuesHolder,heightValuesHolder)

                animateBalloonOnTouchDown()

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                dx = (event.rawX / backgroundRect.right) * 100f

                setProgress(dx.toInt())

                rotate = -30f

                if (event.x < onActionDownPoint) {
                    rotate = 30f
                }
                val rotation = PropertyValuesHolder.ofFloat("angle",rotate,0f)
                val radiusH = PropertyValuesHolder.ofFloat("radius",5f,150f)
                val sizeH = PropertyValuesHolder.ofFloat("size",15f,30f)

                drawableAnimator.setValues(alphaValuesHolder,widthValuesHolder,heightValuesHolder)

                valueAnimator.setValues(rotation,radiusHolder, sizeHolder)
                animateBalloonOnTouchDown()
                animateBalloonAngle()

                return true
            }

            MotionEvent.ACTION_UP -> {
                shouldDraw = false
                shouldAnimateDrawable = false
               // valueAnimator.setValues(radiusHolder, sizeHolder)
                rotate = 0f
                invalidate()
                return true
            }

            else -> return false
        }
    }

}