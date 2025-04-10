package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

/**
 * This fragment is a timer that has play, pause and restart functions.
 *
 * It also allows the user to make use of the proximity sensor to
 * pause the timer.
 *
 * It is also a way of automatically pausing the timer is the user picks up the phone.
 */
class TimerFragment :
    Fragment(),
    View.OnClickListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor

    // The default time selected if user does not choose
    private var timeSelected: Long = 6000
    private var timeToGo: Long = 0

    // Class used to set the state of the timer
    enum class TimerStatus {
        STOPPED,
        STARTED,
        PAUSED,
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var editTime: EditText
    private lateinit var viewTime: TextView
    lateinit var buttonReset: ImageView
    private lateinit var buttonStartStop: ImageView

    var timerStatus: TimerStatus = TimerStatus.STOPPED
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Sensor components are initialised once the fragment is opened
         *
         */
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!
        if (proximitySensor == null) {
            Toast.makeText(requireContext(), "Sensor unavailable", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(proximityListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_timer, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initListeners()
    }

    /**
     * Button listeners are initialised
     */
    private fun initListeners() {
        buttonReset.setOnClickListener(this)
        buttonStartStop.setOnClickListener(this)
    }

    /**
     * Views are then initialised by finding them in the XML file
     *
     */
    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progress_bar)
        editTime = view.findViewById(R.id.edit_text_time)
        viewTime = view.findViewById(R.id.text_view_time)
        buttonReset = view.findViewById(R.id.reset_button)
        buttonStartStop = view.findViewById(R.id.start_stop_button)
    }

    /**
     * Function used to handle what happens after a button is pressed.
     */
    override fun onClick(view: View) {
        when (view.id) {
            R.id.reset_button -> reset()
            R.id.start_stop_button -> startStop()
        }
    }

    /**
     * When function is called the timer is reset
     */
    private fun reset() {
        stopTimer()
        timeToGo = timeSelected
        viewTime.text = timeFormat(timeToGo)
        setProgress()
        timerStatus = TimerStatus.STOPPED
        buttonStartStop.setImageResource(R.drawable.play_blue_button_icon)
        buttonReset.visibility = View.GONE
        editTime.isEnabled = true
    }

    /**
     * Checks the state of the timer then pauses of resumes accordingly
     */
    fun startStop() {
        when (timerStatus) {
            TimerStatus.STOPPED, TimerStatus.PAUSED -> {
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

            TimerStatus.STARTED -> {
                stopTimer()
                buttonStartStop.setImageResource(R.drawable.play_blue_button_icon)
                timerStatus = TimerStatus.PAUSED
            }
        }
    }

    /**
     * Text input is read to then set the length of the timer
     */
    private fun setTimer() {
        var time = editTime.text.toString().toIntOrNull() ?: 1

        timeSelected = (time * 60 * 1000L).coerceAtMost(60 * 60 * 1000L * 50)

        timeToGo = timeSelected
        setProgress()
    }

    /**
     * Starts the timer and updates the UI as needed
     */
    private fun startTimer() {
        countDownTimer =
            object : CountDownTimer(timeToGo, 1000) {
                override fun onTick(timeLeft: Long) {
                    timeToGo = timeLeft
                    viewTime.text = timeFormat(timeToGo)
                    updateProgressBar(timeLeft)
                }

                override fun onFinish() {
                    viewTime.text = timeFormat(0)
                    updateProgressBar(0)
                    buttonReset.visibility = View.GONE

                    buttonStartStop.setImageResource(R.drawable.play_blue_button_icon)
                    editTime.isEnabled = true
                    timerStatus = TimerStatus.STOPPED

                    timeToGo = 0
                }
            }.start()
    }

    /**
     * As timer updates the progress bar state changes
     *
     * (Not working in this implementation)
     */
    private fun updateProgressBar(timeLeft: Long) {
        val progress = ((timeSelected - timeLeft) * 100 / timeSelected).toInt()
        progressBar.progress = progress
    }

    /**
     * Stops the timer from progressing
     */
    private fun stopTimer() {
        countDownTimer?.cancel()
    }

    /**
     * Initialises the progress bar's progress
     */
    private fun setProgress() {
        progressBar.max = 100
        progressBar.progress = 0
    }

    /**
     * The time given in milliseconds is converted to match the UI format
     */
    private fun timeFormat(milliseconds: Long): String {
        val total = milliseconds / 1000
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val seconds = total % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * The proximity sensor automatically pauses the timer when object detected nearby.
     *
     *
     */
    private var proximityListener: SensorEventListener? =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
                    if (event.values[0] == 0f && timerStatus == TimerStatus.STARTED) {
                        Log.d("Sensor value", "Near")
                        stopTimer()
                        timerStatus = TimerStatus.PAUSED
                        showPauseDialog()
                    } else {
                        Log.d("Sensor value", "Far away")
                    }
                }
            }

            override fun onAccuracyChanged(
                p0: Sensor?,
                p1: Int,
            ) {
                // Not used
            }
        }

    /**
     * When timer is paused by proximity sensor, user is notified and
     * option to resume is given.
     *
     * If dialog is dismissed automatically resume the timer
     */
    private fun showPauseDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder
            .setMessage("Timer paused")
            .setPositiveButton("Resume") { _, _ ->
                if (timerStatus == TimerStatus.PAUSED) {
                    startTimer()
                    timerStatus = TimerStatus.STARTED
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.setCancelable(true)

        val dialog = builder.create()

        dialog.setOnDismissListener {
            if (timerStatus == TimerStatus.PAUSED) {
                startTimer()
                timerStatus = TimerStatus.STARTED
            }
        }

        dialog.show()
    }

    /**
     * As user navigates away, timer is destroyed and sensor listeners are unregistered to
     * save resources.
     */
    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        sensorManager.unregisterListener(proximityListener)
    }
}
