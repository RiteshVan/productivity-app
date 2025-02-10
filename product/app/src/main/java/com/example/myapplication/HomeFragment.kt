package com.example.myapplication

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.FragmentTasksBinding
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel


class HomeFragment : Fragment() {



    private lateinit var pieChart: PieChart
    private  var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private lateinit var tasksAdapter:TasksAdapter
    private lateinit var greeting:TextView

    private lateinit var usernameText: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater,container,false)




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        arguments?.let {
            usernameText = it.getString("Username","")

        }
        super.onViewCreated(view, savedInstanceState)

        super.onViewCreated(view, savedInstanceState)

        //Initialise DB and task adapter

        //Shows user message
        greeting = view.findViewById<TextView>(R.id.greeting)
        greeting.text = setGreeting()

        pieChart = binding.pieChart
        setChart()
    }


    private fun setChart(){

        pieChart.animate()

    }

    private fun setGreeting():String{
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hourOfDay) {
            in 0..11 -> "Good Morning!"
            in 12..17 -> "Good Afternoon!"
            else -> "Good Evening!"
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
    }

}