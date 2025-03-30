package com.example.myapplication


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

/**
 * This adapter class is used to display a list of tasks in a recycler view.
 *
 * It also manages some user actions like ticking tasks off and also shows and alert
 * dialog to give the user an option to add an image to the backend.
 *
 */
class TasksAdapter(
    private var tasks: List<Task>,
    private val context: Context,
    private val usernameText: String,
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    //Code used for camera permissions
    private val requestCode = 100

    //Holds task item views
    class TaskViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        val checkBox: CheckBox = itemView.findViewById(R.id.task_check)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_view, parent, false)
        return TaskViewHolder(view)
    }

    //Gets the total number of tasks in the recycler view
    override fun getItemCount(): Int = tasks.size


    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int,
    ) {
        val task = tasks[position]
        holder.titleTextView.text = task.title


        //As users ticks of task dialog to ask to take picture is shown
        holder.checkBox.setOnClickListener {
            Log.d("test", "clicked")
            taskCompletedDialog(task)
        }
    }

    /**
     * Function used to create alert dialog to ask user
     * if they would like to take a photo.
     *
     * If yes the camera is opened
     *
     * If no the task is deleted and user redirected to tasks page
     */
    private fun taskCompletedDialog(task: Task) {
        AlertDialog
            .Builder(context)
            .setTitle("Task picture?")
            .setPositiveButton("Yes") { _, _ ->
                openCamera(task)
            }.setNegativeButton("No") { _, _ ->
                deleteTask(task.id)
            }.show()
    }


    //Function used to open the camera
    private fun openCamera(task: Task) {


        //This is used to locate the tasks fragment to a access its variables
        val tasksFragment =
            (context as? FragmentActivity)?.supportFragmentManager?.fragments?.find {
                it is TasksFragment
            } as? TasksFragment

        //The caption variable is set to the task title before being uploaded to backend
        tasksFragment?.caption = task.title

        //This checks to ensure user has allowed camera to be used
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {

            //This intent is used to open camera
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            //The fragment's camera launcher is used to open
            tasksFragment?.cameraLauncher?.launch(intent)
        } else {
            //If user has not already given permission to use the camera then request to use
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                requestCode,
            )
        }

        //Deletes the task once the camera has been opened
        deleteTask(task.id)
    }

    //Task id is used in order to delete the task from  the backend
    private fun deleteTask(taskId: Int) {

        //Initialises the client
        val client = OkHttpClient()

        //Forms the request for the entry to be deleted from backend
        val request =
            Request
                .Builder()
                .url("http://192.168.1.112:4998/delete_task/$taskId")
                .delete()
                .build()

        //Request is made
        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    //If the connection cannot be made error is shown
                    (context as? FragmentActivity)?.runOnUiThread {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    try {
                        if (response.isSuccessful) {
                            //If connection successful and task deleted message is shown accordingly
                            (context as? FragmentActivity)?.runOnUiThread {
                                Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                                refreshTasks()
                            }
                        } else {
                            //If connection successful but task not deleted message is shown accordingly
                            (context as? FragmentActivity)?.runOnUiThread {
                                Toast.makeText(context, "Task not deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        (context as? FragmentActivity)?.runOnUiThread {
                            Toast.makeText(context, "Response error", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        response.close()
                    }
                }
            },
        )
    }

    //Function is used to obtain most recent tasks from the backend
    fun refreshTasks() {
        val client = OkHttpClient()

        val request = Request.Builder().url("http://192.168.1.112:4998/get_tasks/$usernameText").build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    (context as? FragmentActivity)?.runOnUiThread {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()

                            val newTasks = getTasks(responseBody)

                            (context as? FragmentActivity)?.runOnUiThread {
                                tasks = newTasks
                                notifyDataSetChanged()
                            }
                        } else {
                            (context as? FragmentActivity)?.runOnUiThread {
                                Toast.makeText(context, "Failed to get tasks", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        (context as? FragmentActivity)?.runOnUiThread {
                            Toast.makeText(context, "Response error", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        response.close()
                    }
                }
            },
        )
    }

    // Function used to convert JSON response for the API into task objects
    private fun getTasks(responseBody: String?): List<Task> {
        /**
         * Mutable list initialised so that tasks can be
         * added as they are obtained from the backend
         */
        val tasks = mutableListOf<Task>()

        if (responseBody != null) {
            //Json response is converted to an array
            val json = JSONObject(responseBody)
            val tasksArray = json.getJSONArray("tasks")

            //For loop used to iterate through all the items in array
            for (i in 0 until  tasksArray.length()) {
                //Variables that represent one task are obtained
                val taskObject = tasksArray.getJSONObject(i)
                val task =
                    Task(
                        taskObject.getInt("id"),
                        taskObject.getString("title"),
                        taskObject.getString("tag"),
                        taskObject.getInt("hours"),
                        taskObject.getString("username"),
                    )

                //After object created it is added to the mutable list
                tasks.add(task)
            }
        }
        return tasks
    }

    //Updates the recycler view with new tasks
    fun updateTasks(newTasks :List<Task>){
        tasks=newTasks
        notifyDataSetChanged()
    }
}