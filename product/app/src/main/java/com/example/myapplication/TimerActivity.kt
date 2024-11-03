package com.example.myapplication

import android.icu.text.DecimalFormat
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.TimeUnit

class TimerActivity : AppCompatActivity() {

    lateinit var timeText: TextView
    lateinit var progressBar: ProgressBar


    val countDownTime = 3600
    val clockVal = (countDownTime *1000).toLong()
    val timeProgress = (clockVal/1000).toFloat()

    private val onBackPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            onBackPressedMethod()
        }

    }

    private fun onBackPressedMethod() {
        timerFunction.pauseTimer()
        finish()
    }

    lateinit var timerFunction: TimerFunction

    private fun timeFormat(secondsRemaining: Int, timeText: TextView) {
        progressBar.progress=secondsRemaining

        val decimal =DecimalFormat("00")
        val hour = secondsRemaining/3600
        val minute = (secondsRemaining%3600)/60
        val second =  secondsRemaining%60
        val format = decimal.format(hour) + ":" + decimal.format(minute) + ":" + decimal.format(second)
        timeText.text  = format
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        timeText = findViewById(R.id.textTime)
        progressBar = findViewById(R.id.progressBar)

        var secondsRemaining  = 0
        timerFunction = TimerFunction(clockVal,1000)

        timerFunction.onTick = {millisUntilFinished ->
            val seconds = (millisUntilFinished/1000f).toInt()
            if (seconds != secondsRemaining){
                secondsRemaining = seconds

                timeFormat(secondsRemaining,timeText)
            }
        }

        timerFunction.onFinish={
            timerFunction.running=false

            timeFormat(secondsRemaining,timeText)
        }

        progressBar.max = timeProgress.toInt()
        progressBar.progress = timeProgress.toInt()

        timerFunction.startTimer()

        val btn = findViewById<Button>(R.id.button)

        btn.setOnClickListener{
            if (!timerFunction.running){
                timerFunction.startTimer()
                btn.text="Pause"
            }
            else{
                timerFunction.pauseTimer()
                btn.text="Play"
            }

        }
    }
}
