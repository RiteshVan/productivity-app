package com.example.myapplication

import android.os.CountDownTimer
import android.view.ViewParent

class TimerFunction(
    val millisInFuture: Long,
    val countDownInterval:Long


) {
    var millisUntilFinished = millisInFuture
    private var timer = Timer(this,millisInFuture,countDownInterval)
    var running = false
    var onTick: ((millisUntilFinished:Long) -> Unit)? =null
    var onFinish: (() -> Unit)? = null

    private class Timer(
        val parent: TimerFunction,
        millisInFuture: Long,
        countDownInterval: Long

    ): CountDownTimer(millisInFuture,countDownInterval){

        var millisUntilFinished = parent.millisUntilFinished

        override fun onTick(millisUntilFinished: Long) {
            this.millisUntilFinished = millisUntilFinished
            parent.onTick?.invoke(millisUntilFinished)
        }

        override fun onFinish() {
            millisUntilFinished=0
            parent.onFinish?.invoke()

        }


    }
    fun resumeTimer(){
        if (!running && (timer.millisUntilFinished>0)){
            timer.start()
            running=true
        }
    }

    fun startTimer(){
        timer.start()
        running=true
    }

    fun pauseTimer(){
        timer.cancel()
        running=false
    }
}