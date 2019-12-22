package com.rohith.progressselector

import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customProgressBar.setDrawable(R.drawable.ic_baseline_star_24)

        customProgressBar.listeners = object : DrawableProgressSlider.ProgressBarListeners {
            override fun onProgressStart(view: View?) = Unit
            override fun onProgressChanged(view: View?, value: Float) = Unit
            override fun onProgressCompleted(view: View?) = Unit
        }

    }
}
