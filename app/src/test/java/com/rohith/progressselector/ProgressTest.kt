package com.rohith.progressselector

import junit.framework.TestCase.assertEquals
import org.junit.Test


class ProgressTest {

    private val progressBarWidth = 440

    @Test
    fun testCurrentValue(){
        //assertEquals(25f,getCurrentValueFromTouchPoint(340f,50f))
        assertEquals(25f,getCurrentValueFromTouchPoint(220f,50f))
    }


    private fun getCurrentValueFromTouchPoint(touchPoint: Float, maxValue: Float):Float {
        val value = ((touchPoint/progressBarWidth) )
        return maxValue * value
    }
}