package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter class is used to display leaderboard entries in a recycler view
 * Each entry contains a username, rank and number of hours/points
 *
 * @param leaderboard A pair list containing a string and integer
 */
open class LeaderboardAdapter(
    private var leaderboard: List<Pair<String, Int>>,
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {
    class LeaderboardViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        val rankText: TextView = itemView.findViewById(R.id.rank_text)
        val usernameText: TextView = itemView.findViewById(R.id.username_text)
        val hoursText: TextView = itemView.findViewById(R.id.hours_text)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_item, parent, false)
        return LeaderboardViewHolder(view)
    }

    /**
     * To display key data
     */
    override fun onBindViewHolder(
        holder: LeaderboardViewHolder,
        position: Int,
    ) {
        val (username, hours) = leaderboard[position]

        holder.rankText.text = "${position + 1}"
        holder.usernameText.text = username
        holder.hoursText.text = "$hours hrs"
    }

    /**
     * Returns the total number of items in the leaderboard
     *
     * @return Returns the size as an integer
     */
    override fun getItemCount() = leaderboard.size

    /**
     * Updates the leaderboard with the current leaderboard values as stored in the database
     *
     * @param newLeaderboard Returns the list of leaderboard entries
     *
     */
    fun updateLeaderboard(newLeaderboard: List<Pair<String, Int>>) {
        leaderboard = newLeaderboard
        notifyDataSetChanged()
    }
}
