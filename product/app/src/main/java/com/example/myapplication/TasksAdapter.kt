package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView


class TasksAdapter(private var tasks: List<Task>,context: Context,private val usernameText: String) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    private val db:TaskDatabase = TaskDatabase(context)

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        val checkBox:CheckBox = itemView.findViewById(R.id.task_check)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_view,parent,false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int =tasks.size


    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.titleTextView.text = task.title

        holder.checkBox.setOnClickListener{
            db.deleteTask(task,usernameText)
            refreshData(db.getTasks())
        }
    }

    fun refreshData(newTasks:List<Task>){
        tasks = newTasks
        notifyDataSetChanged()

    }
}