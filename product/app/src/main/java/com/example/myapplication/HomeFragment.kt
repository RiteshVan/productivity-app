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

    private lateinit var db:TaskDatabase
    private lateinit var tasksAdapter:TasksAdapter
    private lateinit var greeting:TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentHomeBinding.inflate(inflater,container,false)




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = TaskDatabase(requireContext())
        tasksAdapter= TasksAdapter(db.getTasks(),requireContext())

        greeting = view.findViewById<TextView>(R.id.greeting)
        greeting.text = setGreeting()

        pieChart = binding.pieChart
        setChart()
    }


    private fun setChart(){

        val workHours = db.getTotalHoursForTag("Work").toFloat()
        val personalHours = db.getTotalHoursForTag("Personal").toFloat()
        val exerciseHours = db.getTotalHoursForTag("Exercise").toFloat()
        val shoppingHours = db.getTotalHoursForTag("Shopping").toFloat()
        val uniWorkHours = db.getTotalHoursForTag("Uni Work").toFloat()
        val gardeningHours = db.getTotalHoursForTag("Gardening").toFloat()

        pieChart.addPieSlice(
            PieModel(
                "Work",workHours,Color.parseColor("#FF0000")
            )
        )

        pieChart.addPieSlice(
            PieModel(
                "Personal",personalHours,Color.parseColor("#FFFF00")
            )
        )

        pieChart.addPieSlice(
            PieModel(
                "Exercise",exerciseHours,Color.parseColor("#FFA500")
            )
        )

        pieChart.addPieSlice(
            PieModel(
                "Shopping",shoppingHours,Color.parseColor("#29B6F6")
            )
        )

        pieChart.addPieSlice(
            PieModel(
                "Uni_Work",uniWorkHours,Color.parseColor("#00008B")
            )
        )

        pieChart.addPieSlice(
            PieModel(
                "Gardening",gardeningHours,Color.parseColor("#008000")
            )
        )

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
        tasksAdapter.refreshData(db.getTasks())
    }

}