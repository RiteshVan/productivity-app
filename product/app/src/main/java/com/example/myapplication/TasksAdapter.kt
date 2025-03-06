package com.example.myapplication


import kotlin.random.Random
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException



class TasksAdapter(private var tasks: List<Task>,private val context: Context,private val usernameText: String) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {
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
            Log.d("test","clicked")
            taskCompletedDialog(task)
        }
    }


    private fun taskCompletedDialog(task: Task) {
        AlertDialog.Builder(context)
            .setTitle("Task picture?")
            .setPositiveButton("Yes"){_,_->
                openCamera(task)
            }.setNegativeButton("No"){_,_->
                deleteTask(task.id)
            }.show()
    }

    private fun openCamera(task: Task){

        val tasksFragment = (context as? FragmentActivity)?.supportFragmentManager?.fragments?.find {
            it is TasksFragment
        } as? TasksFragment

        tasksFragment?.caption = task.title

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("Title",task.title)
            tasksFragment?.cameraLauncher?.launch(intent)
        } else{
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }

        deleteTask(task.id)

    }



    private fun deleteTask(taskId : Int) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.1.112:4998/delete_task/$taskId")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                (context as? FragmentActivity)?.runOnUiThread {
                    Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        (context as? FragmentActivity)?.runOnUiThread {
                            Toast.makeText(context,"Task deleted",Toast.LENGTH_SHORT).show()
                            refreshTasks()
                        }
                    } else {
                        (context as? FragmentActivity)?.runOnUiThread {
                            Toast.makeText(context,"Task not deleted",Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e:Exception) {
                    (context as? FragmentActivity)?.runOnUiThread {
                        Toast.makeText(context, "Response error", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    response.close()
                }

            }
        })

    }




    fun refreshTasks() {
        val client = OkHttpClient()

        val request = Request.Builder().url("http://192.168.1.112:4998/get_tasks").build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                (context as? FragmentActivity)?.runOnUiThread {
                    Toast.makeText(context,"Error",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()

                        val newTasks = getTasks(responseBody)

                        (context as? FragmentActivity)?.runOnUiThread{
                            tasks =newTasks
                            notifyDataSetChanged()
                        }
                    } else {
                        (context as? FragmentActivity)?.runOnUiThread {
                            Toast.makeText(context,"Failed to get tasks",Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    (context as? FragmentActivity)?.runOnUiThread {
                        Toast.makeText(context,"Response error",Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    response.close()
                }
            }

        })

    }


    private fun getTasks(responseBody: String?) : List<Task> {
        val tasks = mutableListOf<Task>()

        if (responseBody != null) {
            val json = JSONObject(responseBody)
            val tasksArray = json.getJSONArray("tasks")

            for (i in 0 until tasksArray.length()) {
                val taskObject = tasksArray.getJSONObject(i)
                val task = Task(
                    taskObject.getInt("id"),
                    taskObject.getString("title"),
                    taskObject.getString("tag"),
                    taskObject.getInt("hours"),
                    taskObject.getString("username")
                )

                tasks.add(task)
            }
        }
        return tasks
    }
}
