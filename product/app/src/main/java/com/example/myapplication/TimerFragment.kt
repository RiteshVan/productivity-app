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
    private var timeToGo:Long =timeSelected

    private enum class TimerStatus{
        STOPPED,STARTED
    }



    private lateinit var view: View

    private var timerStatus:TimerStatus = TimerStatus.STOPPED

    private lateinit var progressBar:ProgressBar
    private lateinit var editTime: EditText
    private lateinit var viewTime:TextView
    private lateinit var buttonReset:ImageView
    private lateinit var buttonStartStop:ImageView

    private lateinit var countDownTimer:CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!
        if (proximitySensor==null){
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
        view = inflater.inflate(R.layout.fragment_timer, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        initListeners()

    }


    private fun initListeners() {
        buttonReset.setOnClickListener(this)
        buttonStartStop.setOnClickListener(this)

    }

    private fun initViews(){
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
        startTimer()
    }


    private fun startStop() {
        if (timerStatus == TimerStatus.STOPPED){
            setTimer()

            setProgress()

            buttonReset.visibility = View.VISIBLE

            buttonStartStop.setImageResource(R.drawable.stop_blue_button_icon)

            editTime.isEnabled = false

            timerStatus = TimerStatus.STARTED

            startTimer()
        }
        else{
            buttonReset.visibility = View.GONE

            buttonStartStop.setImageResource(R.drawable.play_blue_button_icon)

            editTime.isEnabled = true

            timerStatus =TimerStatus.STOPPED

            stopTimer()

        }
    }



    private fun setTimer() {
        var time = 0

        if (editTime.text.toString().isNotEmpty()){

            time = editTime.text.toString().toInt()
        } else {
            Toast.makeText(requireContext(), "Add no. minutes",Toast.LENGTH_SHORT).show()
        }
        timeSelected = time.toLong() * 60 * 1000

    }



    private fun startTimer(){
        countDownTimer = object :CountDownTimer(timeSelected,1000) {
            override fun onTick(timeLeft: Long) {
                viewTime.text = timeFormat(timeLeft)
                progressBar.progress = ((timeSelected - timeLeft)/1000).toInt()
            }

            override fun onFinish() {
                viewTime.text = timeFormat(timeSelected)
                setProgress()
                buttonReset.visibility= View.GONE
                buttonStartStop.setImageResource(R.drawable.play_blue_button_icon)
                editTime.isEnabled=true
                timerStatus= TimerStatus.STOPPED

            }

        }.start()
        countDownTimer.start()

    }

    private fun stopTimer(){
        countDownTimer.cancel()
    }


    private fun setProgress(){
        progressBar.max = (timeSelected/1000).toInt()
        progressBar.progress = (timeSelected/1000).toInt()

    }


    private fun timeFormat(milliseconds:Long):String{
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(minutes)

        val format = String.format("%02d:%02d:%02d",hours,minutes,seconds)

        return format

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
                .setPositiveButton("Resume"){dialog,id -> startTimer()}
                .setNegativeButton("Cancel"){dialog,id -> //Remains paused
                }

            val alert = builder.create()
            alert.show()
        }
    }
}