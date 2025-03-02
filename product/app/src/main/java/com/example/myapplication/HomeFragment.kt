package com.example.myapplication

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.FragmentTasksBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel
import org.json.JSONObject
import java.io.IOException


class HomeFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private  var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private lateinit var tasksAdapter:TasksAdapter
    private lateinit var greeting:TextView

    private lateinit var usernameText: String

    private val client = OkHttpClient()

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

        getHoursPerTag()
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

    private fun getHoursPerTag() {
        val request = Request.Builder().url("http://192.168.1.112:4998/get_hours_per_tag").build()

        client.newCall(request).enqueue(object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Can't get data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody.toString())
                    val hoursPerTag = jsonResponse.getJSONObject("hours_per_tag")

                    val workHours = hoursPerTag.optInt("Work",0).toFloat()
                    val personalHours = hoursPerTag.optInt("Personal", 0).toFloat()
                    val exerciseHours = hoursPerTag.optInt("Exercise", 0).toFloat()
                    val shoppingHours = hoursPerTag.optInt("Shopping", 0).toFloat()
                    val uniWorkHours = hoursPerTag.optInt("Uni_Work", 0).toFloat()
                    val gardeningHours = hoursPerTag.optInt("Gardening", 0).toFloat()

                    requireActivity().runOnUiThread {
                        updatePieChart(workHours,personalHours,exerciseHours,shoppingHours,uniWorkHours,gardeningHours)
                    }


                }
            }


        })

    }

    private fun updatePieChart(workHours: Float,personalHours: Float,exerciseHours: Float,shoppingHours: Float,uniWorkHours: Float,gardeningHours:Float){
        pieChart.clearChart()

        pieChart.addPieSlice(
            PieModel("Work",workHours,Color.parseColor("#FF0000"))
        )

        pieChart.addPieSlice(
            PieModel("Personal",personalHours,Color.parseColor("#FFFF00"))
        )

        pieChart.addPieSlice(
            PieModel("Exercise",exerciseHours,Color.parseColor("#FFA500"))
        )

        pieChart.addPieSlice(
            PieModel("Shopping",shoppingHours,Color.parseColor("#29B6F6"))
        )

        pieChart.addPieSlice(
            PieModel("Uni Work",uniWorkHours,Color.parseColor("#00008B"))
        )

        pieChart.addPieSlice(
            PieModel("Gardening",gardeningHours,Color.parseColor("#008000"))
        )

        pieChart.animate()

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        getHoursPerTag()
    }

}