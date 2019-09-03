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
    private var maxValue: Float = 100f
    private var currentValue: Float = 0f

    var listeners: ProgressBarListeners? = null

    private var shouldDraw = false

    private val valueAnimator = ValueAnimator()
    private var drawableAnimator = ValueAnimator()
    private val rotationAnimator = ValueAnimator()

    private var widthValuesHolder = PropertyValuesHolder.ofInt("drawableW", 0, 60)
    private var reverseWidthValuesHolder = PropertyValuesHolder.ofInt("drawableW", 60, 0)
    private var heightValuesHolder = PropertyValuesHolder.ofInt("drawableH", 20, 180)
    private var reverseHeightValuesHolder = PropertyValuesHolder.ofInt("drawableH", 180, 20)
    private var hopeThisWorksHolder = PropertyValuesHolder.ofInt("hopeThisWorks", 0, 60)
    private var reverseThisWorksHolder = PropertyValuesHolder.ofInt("hopeThisWorks", 60, 0)

    private var rotationAngleLTRHolder = PropertyValuesHolder.ofFloat("rotationAngle", -16f, 0f)
    private var rotationAngleRTLHolder = PropertyValuesHolder.ofFloat("rotationAngle", 16f, 0f)

    private val innerRectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val progressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
    }
    private val outerRectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36.0f
        textAlign = Paint.Align.CENTER

    }

    private val progressBackgroundRect = Rect()
    private val progressRect = Rect()
    private val textRect = Rect()
    private val outerRoundedRect = RectF()
    private val innerRoundedRect = RectF()
    private val rectF = Rect()

    private var text = "0%"

    private val radiusHolder: PropertyValuesHolder =
        PropertyValuesHolder.ofFloat("radius", 150f, 5f)
    private val sizeHolder: PropertyValuesHolder = PropertyValuesHolder.ofFloat("size", 30f, 15f)

    private var radius = 5f
    private var size = 15f

    private var drawableW = 0
    private var drawableH = 0

    private var rotateBalloonBy = 0f

    private var progressBarStart: Int = 0
    private var progressBarHeight: Int = 0

    private var hopeThisWorks = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val balloonDrawable: Drawable = resources.getDrawable(R.drawable.balloon_drawable, null)

    private var downX: Float = 0f
    private var downY: Float = 0f
    private var dx = 0f

    private var isMoving = false

    private val assignedMargins = 40

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = measuredHeight //calculated
        val width = measuredWidth //parent width

        setMeasuredDimension(width, height)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {

        progressBarStart = height - 35
        progressBarHeight = progressBarStart + 5

        text = "${currentValue.toInt()}"

        progressBackgroundRect.left = assignedMargins
        progressBackgroundRect.right = width - assignedMargins
        progressBackgroundRect.top = progressBarStart
        progressBackgroundRect.bottom = progressBarHeight

        progressRect.left = assignedMargins
        progressRect.right =
            if (progressWidth(width - assignedMargins) >= assignedMargins)
                progressWidth(width - assignedMargins)
            else
                assignedMargins
        progressRect.top = progressBarStart
        progressRect.bottom = progressBarHeight

        textPaint.getTextBounds(text, 0, text.length, textRect)

        canvas?.drawRect(progressBackgroundRect, progressBackgroundPaint)
        canvas?.drawRect(progressRect, outerRectPaint)

        val progressMiddle = (progressRect.bottom - progressRect.top) / 2

        outerRoundedRect.left = progressRect.right - size
        outerRoundedRect.right = progressRect.right + size
        outerRoundedRect.top = progressRect.top - size + progressMiddle
        outerRoundedRect.bottom = progressRect.bottom + size - progressMiddle

        var sizeInside = size / 2
        if (shouldDraw)
            sizeInside = size - 3f

        innerRoundedRect.left = progressRect.right - sizeInside
        innerRoundedRect.right = progressRect.right + sizeInside
        innerRoundedRect.top = progressRect.top - sizeInside + progressMiddle
        innerRoundedRect.bottom = progressRect.bottom + sizeInside - progressMiddle

        canvas?.drawRoundRect(outerRoundedRect, radius, radius, outerRectPaint)
        canvas?.drawRoundRect(innerRoundedRect, radius, radius, innerRectPaint)

        if (isMoving) {
            canvas?.save()
            canvas?.rotate(rotateBalloonBy, progressRect.right.toFloat(), (progressRect.top - 30f))
        }
        rectF.left = progressRect.right - drawableW
        rectF.right = progressRect.right + drawableW
        rectF.bottom = progressRect.top - hopeThisWorks
        rectF.top = progressRect.top - drawableH

        val layerList = balloonDrawable as? LayerDrawable
        layerList?.bounds = rectF


        //canvas?.restore()

        if (drawableH < 30 || drawableW < 20/* && rectF.left == progressRect.right */) {
            layerList?.alpha = 0
        } else {
            if (canvas != null)
                balloonDrawable.draw(canvas)
            layerList?.alpha = 255
            canvas?.drawText(
                text,
                progressRect.right.toFloat(),
                rectF.top + ((rectF.bottom - rectF.top) / 2f) + 12f,
                textPaint
            )
        }
        if (isMoving)
            canvas?.restore()

        super.onDraw(canvas)
    }

    override fun performClick(): Boolean {
        super.performClick()

        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {

                downX = event.x
                downY = event.y

                shouldDraw = false
                dx = ((event.rawX / progressBackgroundRect.right))
                val progress = (dx * maxValue)

                if (progress <= 100)
                    setProgress(progress)
                else
                    setProgress(100f)

                val a = PropertyValuesHolder.ofFloat("radius", 5f, 150f)
                val b = PropertyValuesHolder.ofFloat("size", 15f, 30f)
                valueAnimator.setValues(a, b)
                drawableAnimator.setValues(
                    widthValuesHolder,
                    heightValuesHolder,
                    hopeThisWorksHolder
                )

                isMoving = false
                animateRoundedRect()
                animateBalloonOnTouchDown()

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                isMoving = true
                dx = (event.rawX / progressBackgroundRect.right)

                val currentX = event.x
                val currentY = event.y

                if (Math.abs(downX - currentX) > Math.abs(
                        downY - currentY
                    )
                ) {
                    Log.v("", "x")
                    // going backwards: pushing stuff to the right
                    if (downX < currentX) {
                        Log.v("", "right")
                        rotationAnimator.setValues(rotationAngleLTRHolder)
                        rotateBalloonBy = -16f
                        downX = currentX
                    }

                    // going forwards: pushing stuff to the left
                    if (downX > currentX) {
                        Log.v("", "left")
                        rotationAnimator.setValues(rotationAngleRTLHolder)
                        rotateBalloonBy = 16f
                        downX = currentX
                    }

                } else {
                    Log.v("", "y ")

                    if (downY < currentY) {
                        Log.v("", "down")

                    }
                    if (downY > currentY) {
                        Log.v("", "up")

                    }
                }

                val progress = (dx * maxValue)
                if (progress <= 100)
                    setProgress(progress)
                else
                    setProgress(100f)

                return true
            }

            MotionEvent.ACTION_UP -> {
                //isMoving = false
                shouldDraw = false
                valueAnimator.setValues(radiusHolder, sizeHolder)
                animateRoundedRect()
                if (isMoving) {
                    drawableAnimator.setValues(
                        reverseWidthValuesHolder,
                        reverseHeightValuesHolder,
                        reverseThisWorksHolder
                    )
                    animateBalloonOnTouchDown(true)

                    rotationAnimator.duration = 300

                    rotationAnimator.addUpdateListener {
                        rotateBalloonBy = it.getAnimatedValue("rotationAngle") as Float
                        invalidate()
                    }
                    rotationAnimator.start()
                }
                return true
            }

            else -> return false
        }
    }


    private fun animateBalloonOnTouchDown(delayed: Boolean = false) {

        drawableAnimator.duration = 800

        drawableAnimator.addUpdateListener {
            drawableW = it.getAnimatedValue("drawableW") as Int
            drawableH = it.getAnimatedValue("drawableH") as Int
            hopeThisWorks = it.getAnimatedValue("hopeThisWorks") as Int
            invalidate()
        }
        if (delayed) drawableAnimator.startDelay = 800 else drawableAnimator.startDelay = 0
        drawableAnimator.start()
    }

    private fun animateRoundedRect() {
        valueAnimator.cancel()
        valueAnimator.duration = 800

        valueAnimator.addUpdateListener {
            radius = it.getAnimatedValue("radius") as Float
            size = it.getAnimatedValue("size") as Float
            invalidate()
        }

        valueAnimator.start()
    }

    private fun setProgress(value: Float) {

        if (value in minValue..maxValue) {
            if (value == minValue) {
                listeners?.onProgressStart(this)
            }
            currentValue = value


            listeners?.onProgressChanged(this, this.currentValue)

            if (value == maxValue) {
                listeners?.onProgressCompleted(this)
            }
            shouldDraw = true
            invalidate()
        } else {
            throw IllegalArgumentException("Value $value not between $minValue and $maxValue")
        }
    }

    private fun progressWidth(maxWidth: Int): Int {
        val calc = (currentValue * maxWidth) / maxValue
        return calc.toInt()
    }

    interface ProgressBarListeners {

        fun onProgressStart(view: View?)

        fun onProgressChanged(view: View?, value: Float)

        fun onProgressCompleted(view: View?)
    }

}