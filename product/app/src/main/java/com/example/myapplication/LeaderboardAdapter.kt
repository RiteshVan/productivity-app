package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class LeaderboardAdapter(private var leaderboard: List<Pair<String,Int> >) :
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>(){

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val rankText: TextView = itemView.findViewById(R.id.rank_text)
        val usernameText : TextView = itemView.findViewById(R.id.username_text)
        val hoursText:TextView = itemView.findViewById(R.id.hours_text)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_item,parent
            ,false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val (username,hours) = leaderboard[position]

        holder.rankText.text = "${position + 1}"
        holder.usernameText.text= username
        holder.hoursText.text = "$hours hrs"

    }

    override fun getItemCount() = leaderboard.size

    fun updateLeaderboard(newLeaderboard : List<Pair<String,Int>>) {
        leaderboard=newLeaderboard
        notifyDataSetChanged()
    }

}