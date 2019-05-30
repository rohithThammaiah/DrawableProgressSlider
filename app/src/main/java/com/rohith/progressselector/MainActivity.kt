package com.rohith.progressselector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var i =0
       /* var disposable = Observable.interval(
            0,500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                customProgressBar?.setProgress(i++)
            }

        customProgressBar?.listeners = object : IndeterminateProgressBar.ProgressBarListeners{
            override fun onProgressStart(view: View?) {
                Log.e("ProgressBar","Start")
            }

            override fun onProgressChanged(view: View?, value: Int) {
                Log.e("ProgressBar","Running $value")
            }

            override fun onProgressCompleted(view: View?) {
                Log.e("ProgressBar","Completed")
            }

        }*/
    }
}
