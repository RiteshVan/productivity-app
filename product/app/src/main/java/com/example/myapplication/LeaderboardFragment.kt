package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentLeaderboardBinding


class LeaderboardFragment : Fragment() {

    private  var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var db: LoginDetailsDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = LoginDetailsDatabase(requireContext())
        val leaderboard =  db.getLeaderboard()

        val leaderboardView = view.findViewById<RecyclerView>(R.id.leaderboard_view)

        leaderboardAdapter = LeaderboardAdapter(leaderboard)
        leaderboardView.layoutManager = LinearLayoutManager(requireContext())
        leaderboardView.adapter = leaderboardAdapter
    }

}