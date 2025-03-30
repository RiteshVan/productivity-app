package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentLeaderboardBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

//This fragment is used to display the users' scores and their rankings on the leaderboard
class LeaderboardFragment : Fragment() {

    private  var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    lateinit var leaderboardAdapter: LeaderboardAdapter
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val leaderboardView = view.findViewById<RecyclerView>(R.id.leaderboard_view)

        leaderboardAdapter = LeaderboardAdapter(emptyList())
        leaderboardView.layoutManager = LinearLayoutManager(requireContext())
        leaderboardView.adapter = leaderboardAdapter

        getLeaderboard()
    }


    fun getLeaderboard () {
        val request = Request.Builder().url("http://192.168.1.112:4998/get_hours_per_user").build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread{
                    Toast.makeText(requireContext(), "Can't get leaderboard", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    if (responseBody != null){
                    val jsonObject = JSONObject(responseBody.toString())

                    val hoursPerUser = jsonObject.getJSONObject("hours_per_user")

                    val leaderboard = mutableListOf<Pair<String,Int>>()
                    hoursPerUser.keys().asSequence().forEach { username ->
                        val hours = hoursPerUser.optInt(username,0)
                        leaderboard.add(Pair(username,hours))

                    }


                    leaderboard.sortByDescending { it.second }

                    activity?.runOnUiThread {
                        leaderboardAdapter.updateLeaderboard(leaderboard)
                    }
                    }



                }
            }

        })
    }


}