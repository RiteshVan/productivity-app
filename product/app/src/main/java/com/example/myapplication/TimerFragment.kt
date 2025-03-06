package com.example.myapplication


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import java.util.concurrent.TimeUnit


class TimerFragment : Fragment(),View.OnClickListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor


    private var timeSelected:Long = 6000
    private var timeToGo:Long =0

    private enum class TimerStatus{
        STOPPED,STARTED
    }

    private lateinit var progressBar:ProgressBar
    private lateinit var editTime: EditText
    private lateinit var viewTime:TextView
    private lateinit var buttonReset:ImageView
    private lateinit var buttonStartStop:ImageView


    private var timerStatus:TimerStatus = TimerStatus.STOPPED
    private var countDownTimer:CountDownTimer?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!
        if (proximitySensor == null){
            Toast.makeText(requireContext(),"Sensor unavailable",Toast.LENGTH_SHORT).show()
        }
        else{
            sensorManager.registerListener(proximityListener,proximitySensor,SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initListeners()
    }


    private fun initListeners() {
        buttonReset.setOnClickListener(this)
        buttonStartStop.setOnClickListener(this)

    }

    private fun initViews(view: View){
        progressBar = view.findViewById(R.id.progress_bar)
        editTime = view.findViewById(R.id.edit_text_time)
        viewTime = view.findViewById(R.id.text_view_time)
        buttonReset = view.findViewById(R.id.reset_button)
        buttonStartStop = view.findViewById(R.id.start_stop_button)


    }

    override fun onClick(view: View) {
        when (view.id){
            R.id.reset_button-> reset()
            R.id.start_stop_button-> startStop()
        }
    }


    private fun reset(){
        stopTimer()
        timeToGo=timeSelected
        viewTime.text = timeFormat(timeToGo)
        setProgress()
        timerStatus=TimerStatus.STOPPED
        buttonStartStop.setImageResource(R.drawable.play_blue_button_icon)
        buttonReset.visibility = View.GONE
        editTime.isEnabled = true

    }


    private fun startStop() {
        if (timerStatus == TimerStatus.STOPPED){
            if (timeToGo == 0L) {
                setTimer()
            }
            setProgress()

            buttonReset.visibility = View.VISIBLE

            buttonStartStop.setImageResource(R.drawable.stop_blue_button_icon)

            editTime.isEnabled = false

            timerStatus = TimerStatus.STARTED

            startTimer()
        }
        else{
            stopTimer()
            buttonStartStop.setImageResource(R.drawable.play_blue_button_icon)

            timerStatus =TimerStatus.STOPPED

        }
    }



    private fun setTimer() {
        var time = editTime.text.toString().toIntOrNull() ?: 1

        timeSelected = time.toLong() * 60 * 1000

        timeToGo = timeSelected
        setProgress()
    }



    private fun startTimer(){
        countDownTimer = object :CountDownTimer(timeToGo,1000) {
            override fun onTick(timeLeft: Long) {
                timeToGo = timeLeft
                viewTime.text = timeFormat(timeToGo)
                updateProgressBar(timeLeft)
            }

            override fun onFinish() {
                viewTime.text = timeFormat(0)
                updateProgressBar(0)
                buttonReset.visibility= View.GONE

                buttonStartStop.setImageResource(R.drawable.play_blue_button_icon)
                editTime.isEnabled=true
                timerStatus= TimerStatus.STOPPED

                timeToGo=0
            }

        }.start()


    }

    private fun updateProgressBar(timeLeft:Long){
        val progress = ((timeSelected-timeLeft)/1000).toInt()
        progressBar.progress = progress
    }

    private fun stopTimer(){
        countDownTimer?.cancel()
    }


    private fun setProgress(){
        progressBar.max = 100
        progressBar.progress = 0

    }


    private fun timeFormat(milliseconds:Long):String{
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(minutes)

        return String.format("%02d:%02d:%02d",hours,minutes,seconds)

    }

    private var proximityListener : SensorEventListener? = object:SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0f){
                    Log.d("Sensor value","Near")
                    showPauseDialog()
                }
                else{
                    Log.d("Sensor value","Far away")
                }

            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            //Not used
        }


    }

    private fun showPauseDialog() {
        if (timerStatus == TimerStatus.STARTED){
            stopTimer()
            timerStatus=TimerStatus.STOPPED
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Timer paused as phone was moved. Resume?")
                .setPositiveButton("Resume"){_,_ -> startTimer()}
                .setNegativeButton("Cancel"){dialog,_ -> dialog.dismiss() }

            val alert = builder.create()
            alert.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        sensorManager.unregisterListener(proximityListener)
    }
}