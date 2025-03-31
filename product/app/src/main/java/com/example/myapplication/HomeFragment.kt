package com.example.myapplication

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentHomeBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel
import org.json.JSONObject
import java.io.IOException

/**
 * This fragment is the home fragment.
 * The first page the user sees upon login.
 * Gives the user a visual breakdown of how they have spent time on each task
 */
class HomeFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var greeting: TextView

    private lateinit var usernameText: String

    var client = OkHttpClient()

    private var listTaskTitles = mutableListOf<String>()

    private lateinit var priorityView: TextView

    // These values are used to represent time ranges
    // The ranges are used to decide the greeting shown to the user
    private val MORNING_END = 11
    private val EVENING_END = 15

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        // Username is obtained
        arguments?.let {
            usernameText = it.getString("username", "")
        }
        super.onViewCreated(view, savedInstanceState)

        super.onViewCreated(view, savedInstanceState)

        priorityView = view.findViewById(R.id.priority_tasks)

        // Shows user message
        greeting = view.findViewById(R.id.greeting)
        greeting.text = setGreeting(Calendar.getInstance())

        // Generates the chart for the user
        pieChart = binding.pieChart
        setChart()

        // Populates the chart with relevant values
        getHoursPerTag()

        getPrioritisedTasks()
    }

    private fun setChart() {
        pieChart.animate()
    }

    /**
     * Function used to set greeting based on time of day
     *
     * @return a greeting
     */
    fun setGreeting(calendar: Calendar): String {
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hourOfDay) {
            // During morning hours
            in 0..(MORNING_END) -> "Good Morning!"

            // During evening hours
            in (MORNING_END + 1)..EVENING_END -> "Good Afternoon!"

            // Other hours show evening greeting
            else -> "Good Evening!"
        }
    }

    /**
     * Function used to make call to backend to obtain values for each tag and update pie chart
     */
    fun getHoursPerTag() {
        val request = Request.Builder().url("http://192.168.1.112:4998/get_hours_per_tag/$usernameText").build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Can't get data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val jsonResponse = JSONObject(responseBody.toString())
                        val hoursPerTag = jsonResponse.getJSONObject("hours_per_tag")

                        val workHours = hoursPerTag.optInt("Work", 0).toFloat()
                        val personalHours = hoursPerTag.optInt("Personal", 0).toFloat()
                        val exerciseHours = hoursPerTag.optInt("Exercise", 0).toFloat()
                        val shoppingHours = hoursPerTag.optInt("Shopping", 0).toFloat()
                        val uniWorkHours = hoursPerTag.optInt("Uni_Work", 0).toFloat()
                        val gardeningHours = hoursPerTag.optInt("Gardening", 0).toFloat()

                        requireActivity().runOnUiThread {
                            updatePieChart(workHours, personalHours, exerciseHours, shoppingHours, uniWorkHours, gardeningHours)
                        }
                    }
                }
            },
        )
    }

    /**
     * Uses the values obtained from the backend to update the pie chart
     *
     * @param workHours Hours spent on work
     * @param personalHours Hours spent on personal tasks
     * @param exerciseHours Hours spent on exercise
     * @param shoppingHours Hours spent on shopping
     * @param uniWorkHours Hours spent on university work
     * @param gardeningHours Hours spent gardening
     *
     */
    fun updatePieChart(
        workHours: Float,
        personalHours: Float,
        exerciseHours: Float,
        shoppingHours: Float,
        uniWorkHours: Float,
        gardeningHours: Float,
    ) {
        pieChart.clearChart()

        pieChart.addPieSlice(
            PieModel("Work", workHours, Color.parseColor("#FF0000")),
        )

        pieChart.addPieSlice(
            PieModel("Personal", personalHours, Color.parseColor("#FFFF00")),
        )

        pieChart.addPieSlice(
            PieModel("Exercise", exerciseHours, Color.parseColor("#FFA500")),
        )

        pieChart.addPieSlice(
            PieModel("Shopping", shoppingHours, Color.parseColor("#29B6F6")),
        )

        pieChart.addPieSlice(
            PieModel("Uni Work", uniWorkHours, Color.parseColor("#00008B")),
        )

        pieChart.addPieSlice(
            PieModel("Gardening", gardeningHours, Color.parseColor("#008000")),
        )

        pieChart.animate()
    }

    private fun getPrioritisedTasks() {
        val request =
            Request
                .Builder()
                .url("http://192.168.1.112:4998/get_prioritised_tasks/$usernameText")
                .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to get the tasks", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val json = JSONObject(responseBody.toString())
                        val array = json.getJSONArray("tasks")
                        listTaskTitles.clear()

                        // This gets up to first 5 values from database
                        for (i in 0 until minOf(5, array.length())) {
                            listTaskTitles.add(array.getJSONObject(i).getString("title"))
                        }
                    }

                    requireActivity().runOnUiThread {
                        priorityView.text = listTaskTitles.joinToString(separator = "\n")
                    }
                }
            },
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // As fragment is reopened data is refreshed to match any changes to database
    override fun onResume() {
        super.onResume()
        getHoursPerTag()
        getPrioritisedTasks()
    }
}
