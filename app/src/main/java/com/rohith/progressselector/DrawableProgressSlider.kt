package com.rohith.progressselector

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlin.math.abs

class DrawableProgressSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var minValue: Float = 0f
    private var maxValue: Float = 100f
    private var currentValue: Float = 0f

    var listeners: ProgressBarListeners? = null

    private val textRect = Rect()
    private val drawableRect = Rect()
    private val progressRect = Rect()
    private val innerRoundedRect = RectF()
    private val outerRoundedRect = RectF()
    private val progressBackgroundRect = Rect()

    private var dx = 0f
    private var text = "0%"
    private var size = 15f
    private var diffX = 0f
    private var downX = 0f
    private var downY = 0f
    private var radius = 5f
    private var isMoving = false
    private var drawableW = 0
    private var drawableH = 0
    private var shouldDraw = false
    private var scaleValue = 0
    private val assignedMargins = 40
    private var rotateBalloonBy = 0f
    private var progressBarStart: Int = 0
    private var progressBarHeight: Int = 0

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_balloon)

    private val testAnimator = ValueAnimator()
    private val valueAnimator = ValueAnimator()
    private val drawableAnimator = ValueAnimator()
    private val rotationAnimator = ValueAnimator()

    private val widthValuesHolder = PropertyValuesHolder.ofInt("drawableW", 0, 60)
    private val reverseWidthValuesHolder = PropertyValuesHolder.ofInt("drawableW", 60, 0)
    private val heightValuesHolder = PropertyValuesHolder.ofInt("drawableH", 20, 180)
    private val reverseHeightValuesHolder = PropertyValuesHolder.ofInt("drawableH", 180, 20)
    private val scaleValueHolder = PropertyValuesHolder.ofInt("scaleValue", 0, 60)
    private val reverseScaleValueHolder = PropertyValuesHolder.ofInt("scaleValue", 60, 0)
    private val rotationAngleLTRHolder = PropertyValuesHolder.ofFloat("rotationAngle", -8f, 0f)
    private val rotationAngleRTLHolder = PropertyValuesHolder.ofFloat("rotationAngle", 8f, 0f)
    private val radiusHolder = PropertyValuesHolder.ofFloat("radius", 150f, 5f)
    private val reverseRadiusHolder = PropertyValuesHolder.ofFloat("radius", 5f, 150f)
    private val sizeHolder = PropertyValuesHolder.ofFloat("size", 30f, 15f)
    private val reverseSizeHolder = PropertyValuesHolder.ofFloat("size", 15f, 30f)

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val height = measuredHeight //calculated
        val width = measuredWidth //parent width

        setMeasuredDimension(width, height)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas?) {

        progressBarStart = height - 35 // TODO rename
        progressBarHeight = progressBarStart + 5 // height of progress bar TODO rename

        text = "${currentValue.toInt()}"

        progressBackgroundRect.left = assignedMargins
        progressBackgroundRect.top = progressBarStart
        progressBackgroundRect.right = width - assignedMargins
        progressBackgroundRect.bottom = progressBarHeight

        progressRect.left = assignedMargins
        progressRect.right =
            if (progressWidth(width - assignedMargins) >= assignedMargins)
                progressWidth(width - assignedMargins)
            else
                assignedMargins
        progressRect.top = progressBarStart
        progressRect.bottom = progressBarHeight

        textPaint.textSize =
            abs((drawable?.bounds?.width() ?: 0) + (drawable?.bounds?.height() ?: 0)) / 8f

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

        drawableRect.left = progressRect.right - drawableW
        drawableRect.right = progressRect.right + drawableW
        drawableRect.bottom = progressRect.top - scaleValue
        drawableRect.top = progressRect.top - drawableH

        if (isMoving) {
            canvas?.save()
            canvas?.rotate(
                rotateBalloonBy,
                drawableRect.left.toFloat(),
                drawableRect.bottom.toFloat()
            )
        }

        val layerList = drawable
        layerList?.bounds = drawableRect


        //canvas?.restore()

        if (drawableH < 30 || drawableW < 20/* && drawableRect.left == progressRect.right */) {
            layerList?.alpha = 0
        } else {
            if (canvas != null)
                drawable?.draw(canvas) ?: throw IllegalStateException("drawable is null")
            layerList?.alpha = 255
            canvas?.drawText(
                text,
                progressRect.right.toFloat(),
                drawableRect.top + ((drawableRect.bottom - drawableRect.top) / 2f) + 10f,
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
                dx = (downX / (progressBackgroundRect.right - (assignedMargins + 5)))
                val progress = (dx * maxValue)

                if (progress <= 100)
                    setProgress(progress)
                else
                    setProgress(100f)

                valueAnimator.setValues(reverseRadiusHolder, reverseSizeHolder)
                drawableAnimator.setValues(
                    widthValuesHolder,
                    heightValuesHolder,
                    scaleValueHolder
                )

                isMoving = false
                animateRoundedRect()
                animateBalloonOnTouchDown()

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                isMoving = true
                dx = (event.rawX / progressBackgroundRect.right)
                diffX = event.rawX - downX

                Log.e("diffX", "$diffX - $touchSlop")

                if (abs(diffX) > touchSlop) {

                    val currentX = event.x
                    val currentY = event.y

                    if (abs(downX - currentX) > abs(downY - currentY)) {
                        if (downX < currentX) { // scrolling right
                            rotationAnimator.setValues(rotationAngleLTRHolder)
                            // rotateBalloonBy = -8f // balloon rotated to the left
                        }

                        if (downX > currentX) { // scrolling left
                            rotationAnimator.setValues(rotationAngleRTLHolder)
                            // rotateBalloonBy = 8f// balloon rotated to the right
                        }

                        downX = currentX
                    }

                    val progress = (dx * maxValue)
                    if (progress <= 100)
                        setProgress(progress)
                    else
                        setProgress(100f)
                }

                return true
            }

            MotionEvent.ACTION_UP -> {
                shouldDraw = false
                valueAnimator.setValues(radiusHolder, sizeHolder)
                animateRoundedRect()
                if (isMoving) {
                    /*rotationAnimator.duration = 600
                    rotationAnimator.interpolator = DecelerateInterpolator()
                    rotationAnimator.addUpdateListener {
                        rotateBalloonBy = it.getAnimatedValue("rotationAngle") as Float
                        invalidate()
                    }
                    rotationAnimator.doOnEnd {
                        // animateBalloonOnTouchDown(500)

                    }
                    rotationAnimator?.start()*/
                    drawableAnimator.reverse()
                } else {
                    testAnimator.setValues(
                        reverseWidthValuesHolder,
                        reverseHeightValuesHolder,
                        reverseScaleValueHolder
                    )
                    testAnimator.addUpdateListener {
                        drawableW = it.getAnimatedValue("drawableW") as Int
                        drawableH = it.getAnimatedValue("drawableH") as Int
                        scaleValue = it.getAnimatedValue("scaleValue") as Int
                        invalidate()
                    }
                    testAnimator.duration = 1000
                    testAnimator.interpolator = DecelerateInterpolator()
                    testAnimator.startDelay = 1900
                    testAnimator.start()
                }
                return true
            }

            else -> return false
        }
    }

    private fun animateBalloonOnTouchDown(delay: Int = 0) {

        drawableAnimator.duration = 800

        drawableAnimator.addUpdateListener {
            drawableW = it.getAnimatedValue("drawableW") as Int
            drawableH = it.getAnimatedValue("drawableH") as Int
            scaleValue = it.getAnimatedValue("scaleValue") as Int
            invalidate()
        }
        drawableAnimator.startDelay = delay.toLong()

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

    private fun setProgress(value1: Float) {

        val value = abs(value1)

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

    fun setDrawable(@DrawableRes _drawable: Int = R.drawable.ic_balloon) {
        drawable = ContextCompat.getDrawable(context, _drawable)
            ?: throw java.lang.IllegalArgumentException("drawable not found")
        invalidate()
    }


    interface ProgressBarListeners {

        fun onProgressStart(view: View?)

        fun onProgressChanged(view: View?, value: Float)

        fun onProgressCompleted(view: View?)
    }

}