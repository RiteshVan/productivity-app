package com.example.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentTasksBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

/**
 * This fragment displays users' tasks
 * The users can add and delete tasks
 * Can filter tasks by tag
 * When ticking off a task, they can take a picture of a task they have completed
 */
class TasksFragment : Fragment() {
    private val client = OkHttpClient()

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var selectedTag: String
    private lateinit var selectedTime: String

    private lateinit var usernameText: String
    private lateinit var selectedDueDate: String

    /**
     * This variable in particular needs to be visible for the tasks adapter
     *
     * This is so that the tasks adapter can update this variable to have the tasks title
     *
     * As the camera and upload functionality is held in this fragment, this variable is
     * needed to be able to upload the correct caption to the backend.
     *
     * This process cant be completed through the tasks adapter.
     */
    var caption: String? = null

    // This handler is used to execute tasks
    // Important to ensure not too many calls made to the backend at once
    private val handler = Handler(Looper.getMainLooper())

    // Creates the variable for the camera action so that it can be called later
    // Not private so that task adapter can use variable
    lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gets username
        usernameText = ""
        arguments?.let {
            usernameText = it.getString("username", "")
        }

        // Used to check if username is loaded in correctly
        Log.d("track", usernameText)

        /**
         * This is an activity launcher to take a picture
         * Important for this to be defined in the fragment, so that the adapter can access it.
         */
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Image taken by camera is obtained as bitmap
                    val image = result.data?.extras?.get("data") as? Bitmap

                    /**
                     * Uses the task title as caption if variable passed as intended
                     * or else placeholder caption used.
                     */
                    val taskTitle = caption ?: "No caption"

                    Log.d("tasktest", "$taskTitle")

                    // If image is present, it is uploaded along with the caption
                    if (image != null) {
                        uploadImage(image, caption.toString())
                    }
                }
            }
    }

    /**
     * The image and the caption are uploaded to the backend
     *
     * Image bitmap converted to bytes so that it can be uploaded and stored in
     * the backend
     */
    private fun uploadImage(
        bitmap: Bitmap,
        taskTitle: String,
    ) {
        // A random number is generated up to 9999
        // This ensures that the filename is unique
        val randomNumber = Random.nextLong(10000)

        // The bitmap is transformed to a byte sequence so that it can be uploaded to the backend
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()

        // A request body is created so that only jpegs can be uploaded to the database, for security reasons
        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())

        /**
         * A multipart form is needed for this situation as a variety of types need
         * to be uploaded in one request.
         *
         * An image, caption and username are sent to the backend to be stored
         * as an image diary object.
         */
        val multipartBody =
            MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "task-image$randomNumber.jpeg", requestBody)
                .addFormDataPart("caption", taskTitle)
                .addFormDataPart("username", usernameText)
                .build()

        // The request is created for image upload
        val request =
            Request
                .Builder()
                .url("http://192.168.1.112:4997/upload")
                .post(multipartBody)
                .build()

        // Request is executed
        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    response.close()
                }
            },
        )
    }

    // This function is used to select a tag to filter the tasks shown to the user
    private fun showTagSelectedDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.tag_category_selection_dialog, null)
        val tag = dialogLayout.findViewById<Spinner>(R.id.select_tag)

        val listTags = listOf("All", "Work", "Exercise", "Personal", "Shopping", "Uni Work", "Gardening")

        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listTags)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

        tag.adapter = arrayAdapter

        builder
            .setView(dialogLayout)
            .setTitle("Select Tag")
            .setPositiveButton("OK") { _, _ ->
                val selectedTag = tag.selectedItem.toString()
                filterTasksByTag(selectedTag)
            }.setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        tasksAdapter = TasksAdapter(emptyList(), requireContext(), usernameText)

        val tasksView = binding.tasksView

        tasksView.layoutManager = LinearLayoutManager(requireContext())

        tasksView.adapter = tasksAdapter

        refreshTasksList()

        editTaskDialog()

        // When button is selected option to choose category is given
        binding.selectFilter.setOnClickListener {
            showTagSelectedDialog()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    // Dialog popup to add task details
    // All details are then sent to the backend to be stored as task objects
    private fun editTaskDialog() {
        binding.taskAdd.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.new_task_add_dialog, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.task_add_text)
            val dueDateButton = dialogLayout.findViewById<Button>(R.id.select_due_date_button)

            // Choose tag from spinner
            val tag = dialogLayout.findViewById<Spinner>(R.id.select_tag)
            val listTags = listOf("Work", "Exercise", "Personal", "Shopping", "Uni Work", "Gardening")

            val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listTags)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

            tag.adapter = arrayAdapter

            tag.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        selectedTag = parent.getItemAtPosition(position).toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Used to handle case for no selection
                        // Not needed for this implementation
                    }
                }

            // To select the amount hours a task will take
            val time = dialogLayout.findViewById<Spinner>(R.id.select_hours)
            val listHours = listOf("1", "2", "3", "4", "5")

            val arrayAdapterHours = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listHours)
            arrayAdapterHours.setDropDownViewResource(android.R.layout.simple_spinner_item)

            time.adapter = arrayAdapterHours

            time.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        selectedTime = parent.getItemAtPosition(position).toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Used to handle case for no selection
                        // Not needed for this implementation
                    }
                }

            // Used to show calendar view so user can pick a due date
            dueDateButton.setOnClickListener {
                showDatePickerDialog { date ->
                    selectedDueDate = date
                }
            }

            // This section of code is used to monitor when a user is typing
            editText.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        p0: CharSequence?,
                        p1: Int,
                        p2: Int,
                        p3: Int,
                    ) {
                        // Not needed fot this implementation
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        // Not needed fot this implementation
                    }

                    /**
                     * After the user has stopped typing for 3 seconds a call
                     * is made to the backend.
                     *
                     * This 3 second time is used to ensure that the server is
                     * not overloaded.
                     *
                     * As this is a machine learning based request this is crucial to ensure
                     * the server does not overheat (which can lead to permanent damage)
                     */
                    override fun afterTextChanged(p0: Editable?) {
                        handler.removeCallbacksAndMessages(null)

                        handler.postDelayed({
                            classifyTask(editText.text.toString(), tag)
                        }, 3000)
                    }
                },
            )

            with(builder) {
                /**
                 * As user presses this button, the details entered are used to
                 * create tasks objects in the backend.
                 */
                setTitle("Add Task")
                setPositiveButton("OK") { dialog, which ->
                    val title = editText.text.toString()

                    /**
                     * Random number up to 9999 is used to ensure task IDs are
                     * kept unique.
                     *
                     * Particularly important for task deletion.
                     */
                    val randomNumber = Random.nextLong(10000)
                    val uniqueID = "$randomNumber"

                    // Form created storing all details needed for task
                    val formBody =
                        FormBody
                            .Builder()
                            .add("id", uniqueID)
                            .add("title", title)
                            .add("tag", selectedTag)
                            .add("hours", selectedTime)
                            .add("username", usernameText)
                            .add("due_date", selectedDueDate)
                            .build()

                    // Request is formed
                    val request =
                        Request
                            .Builder()
                            .url("http://192.168.1.112:4998/add_task")
                            .post(formBody)
                            .build()

                    // Request is made to the backend
                    client.newCall(request).enqueue(
                        object : Callback {
                            override fun onFailure(
                                call: Call,
                                e: IOException,
                            ) {
                                // If unable to contact backend an error message is shown
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onResponse(
                                call: Call,
                                response: Response,
                            ) {
                                try {
                                    if (response.isSuccessful) {
                                        requireActivity().runOnUiThread {
                                            // If call to backend is successful confirmation message is shown
                                            Toast
                                                .makeText(
                                                    requireContext(),
                                                    "added",
                                                    Toast.LENGTH_SHORT,
                                                ).show()

                                            tasksAdapter.refreshTasks()
                                        }
                                    }
                                } catch (e: Exception) {
                                    // If backend is reached but there is an error, this message is shown
                                    Toast
                                        .makeText(
                                            requireContext(),
                                            "Response error",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                } finally {
                                    response.close()
                                }
                            }
                        },
                    )
                }
                // User can choose to cancel task addition
                // This decision is logged
                setNegativeButton("Cancel") { dialog, which ->
                    Log.d("Tasks", "cancelled")
                }
                setView(dialogLayout)
                show()
            }
        }
    }

    // Function that is used to show the calendar to the user
    // Also selects date and formats it as needed for backend
    private fun showDatePickerDialog(dateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSelector =
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val date =
                        Calendar.getInstance().apply {
                            set(year, month, day)
                        }
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    val finalDate = dateFormat.format(date.time)
                    dateSelected(finalDate)
                },
                year,
                month,
                day,
            )

        dateSelector.show()
    }

    // Refresh tasks from backend
    private fun refreshTasksList() {
        val request = Request.Builder().url("http://192.168.1.112:4998/get_tasks/$usernameText").build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    // Checks to make sure that activity is attached to fragment
                    // Used to ensure there are no error messages when running tests
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    try {
                        if (response.isSuccessful) {
                            requireActivity().runOnUiThread {
                                tasksAdapter.refreshTasks()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to fetch tasks", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Response error", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        response.close()
                    }
                }
            },
        )
    }

    // Task is classified and the correct category is selected automatically for the user
    private fun classifyTask(
        taskTitle: String,
        spinner: Spinner,
    ) {
        // Task title sent to backend for classification
        val formBody = FormBody.Builder().add("value", taskTitle).build()

        // Request is formed
        val request =
            Request
                .Builder()
                .url("http://192.168.1.112:4999/classify")
                .post(formBody)
                .build()

        // Request made to the backend
        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    // If the call does not reach the backend an error message is shown to the user
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    // If the classification is successful the tag is automatically selected from the spinner
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "Work"

                        val tagsList = listOf("Work", "Exercise", "Personal", "Shopping", "Uni Work", "Gardening")

                        val selectedPosition = tagsList.indexOf(responseBody)

                        requireActivity().runOnUiThread {
                            spinner.setSelection(selectedPosition)
                        }
                    } else {
                        // If the classification process is unsuccessful the user is notified
                        Toast.makeText(requireContext(), "Classification failed", Toast.LENGTH_LONG).show()
                    }
                }
            },
        )
    }

    // As the user selects a category tag from the spinner only tasks of that category are obtained and shown
    private fun filterTasksByTag(tag: String) {
        if (tag == "All") {
            refreshTasksList()
        } else {
            val request = Request.Builder().url("http://192.168.1.112:4998/get_tasks_by_tag/$tag").build()

            client.newCall(request).enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        try {
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string()

                                if (responseBody != null) {
                                    val tasks = getTasks(responseBody)
                                    requireActivity().runOnUiThread {
                                        tasksAdapter.updateTasks(tasks)
                                    }
                                } else {
                                    requireActivity().runOnUiThread {
                                        Toast.makeText(requireContext(), "Empty task list", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), "Cannot get tasks", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            requireActivity().runOnUiThread {
                                Log.d("check", "$e")
                                Toast.makeText(requireContext(), "Response error: $e", Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            response.close()
                        }
                    }
                },
            )
        }
    }

    // Function used to convert JSON response from the API into task objects
    private fun getTasks(responseBody: String?): List<Task> {
        /**
         * Mutable list initialised so that tasks can be
         * added as they are obtained from the backend
         */
        val tasks = mutableListOf<Task>()

        if (responseBody != null) {
            // Json response is converted to an array
            val json = JSONObject(responseBody)
            val tasksArray = json.getJSONArray("tasks")

            // For loop used to iterate through all the items in array
            for (i in 0 until tasksArray.length()) {
                // Variables that represent one task are obtained
                val taskObject = tasksArray.getJSONObject(i)

                // The needed details are extracted to create the task object
                val task =
                    Task(
                        taskObject.getInt("id"),
                        taskObject.getString("title"),
                        taskObject.getString("tag"),
                        taskObject.getInt("hours"),
                        taskObject.getString("username"),
                    )

                // After object created it is added to the mutable list
                tasks.add(task)
            }
        }

        return tasks
    }
}
