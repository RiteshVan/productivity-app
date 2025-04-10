package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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

/**
 * This fragment shows users the number of hours that they have worked for and their rank overall
 *
 * Data is fetched from the backend before being displayed
 */
class LeaderboardFragment : Fragment() {
    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    // Adapter is used to display data in the recycler view
    lateinit var leaderboardAdapter: LeaderboardAdapter

    // Client used to make backend requests
    var client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val leaderboardView = view.findViewById<RecyclerView>(R.id.leaderboard_view)

        leaderboardAdapter = LeaderboardAdapter(emptyList())
        leaderboardView.layoutManager = LinearLayoutManager(requireContext())
        leaderboardView.adapter = leaderboardAdapter

        getLeaderboard()
    }

    /**
     * Details from leaderboard are obtained before using details to update the recycler viw
     *
     */
    fun getLeaderboard() {
        val request = Request.Builder().url("http://192.168.1.112:4998/get_hours_per_user").build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Can't get leaderboard", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()

                        if (responseBody != null) {
                            val jsonObject = JSONObject(responseBody.toString())

                            val hoursPerUser = jsonObject.getJSONObject("hours_per_user")

                            //Mutable list is used to keep data pairs of usernames and their total hours
                            val leaderboard = mutableListOf<Pair<String, Int>>()

                            //Iterate through the data and update the list
                            hoursPerUser.keys().asSequence().forEach { username ->
                                val hours = hoursPerUser.optInt(username)
                                leaderboard.add(Pair(username, hours))
                            }

                            //Descending order of hours
                            leaderboard.sortByDescending { it.second }

                            // Update the leaderboard
                            activity?.runOnUiThread {
                                leaderboardAdapter.updateLeaderboard(leaderboard)
                            }
                        }
                    }
                }
            },
        )
    }
}
