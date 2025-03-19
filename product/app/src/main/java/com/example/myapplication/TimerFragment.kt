package com.example.myapplication

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment

class TimerFragment : Fragment() {
    private lateinit var spinner: Spinner
    private lateinit var countDownTimer: CountDownTimer
    private var timerRunning = false

    private lateinit var textViewTimer: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var stopStartPause: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)
        textViewTimer = view.findViewById(R.id.timeLeft)
        progressBar = view.findViewById(R.id.time_progress)
        stopStartPause = view.findViewById(R.id.start_stop_button)

        spinner = view.findViewById(R.id.spinner_time)

        val listItems = listOf(15, 25, 30, 45, 60)

        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listItems)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinner.adapter = arrayAdapter

        val timeChosen = spinner.selectedItem

        progressBar.max = 100

        return view
    }
}
