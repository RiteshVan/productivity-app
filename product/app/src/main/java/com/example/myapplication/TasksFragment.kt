package com.example.myapplication


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentTasksBinding
import com.google.android.material.color.utilities.ToneDeltaPair
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Date
import kotlin.random.Random


class TasksFragment : Fragment() {

    private val client = OkHttpClient()

    private  var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var tasksAdapter:TasksAdapter
    private lateinit var selectedTag:String
    private lateinit var selectedTime:String

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                    val taskTitle = result.data?.extras?.get("Title") as? String

                    Log.d("tasktest","$taskTitle")

                    imageBitmap?.let {
                        uploadImage(it, taskTitle.toString())
                    }

                }

            }
    }

    private fun uploadImage(bitmap: Bitmap,taskTitle: String){

        val randomNumber = Random.nextLong(10000) //Selects random number up to 99


        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
        val byteArray = stream.toByteArray()

        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image","task-image$randomNumber.jpeg", requestBody)
            .addFormDataPart("caption",taskTitle)
            .build()

        val request = Request.Builder().url("http://192.168.1.112:4997/upload").post(multipartBody).build()


        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                }
                response.close()
            }
        })
        }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentTasksBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        tasksAdapter = TasksAdapter(emptyList(),requireContext(),"testUser",cameraLauncher)

        val tasksView = binding.tasksView

        tasksView.layoutManager = LinearLayoutManager(requireContext())

        tasksView.adapter = tasksAdapter

        refreshTasksList()


        editTaskDialog()
    }

    override fun onResume() {
        super.onResume()

    }


    //Dialog popup to add task details
    private fun editTaskDialog(){
        binding.taskAdd.setOnClickListener(){
            val builder =AlertDialog.Builder(requireContext())
            val inflater = layoutInflater
            val dialogLayout =inflater.inflate(R.layout.new_task_add_dialog,null)
            val editText = dialogLayout.findViewById<EditText>(R.id.task_add_text)

            //Choose tag from spinner
            val tag = dialogLayout.findViewById<Spinner>(R.id.select_tag)
            val listTags = listOf("Work","Exercise","Personal","Shopping","Uni  Work","Gardening")

            val arrayAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,listTags)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

            tag.adapter= arrayAdapter

            tag.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedTag = parent.getItemAtPosition(position).toString()

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //Used to handle case for no selection
                    //Not needed for this implementation

                }

            }


            val time =  dialogLayout.findViewById<Spinner>(R.id.select_hours)

            val listHours = listOf("1","2","3","4","5")

            val arrayAdapterHours = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,listHours)
            arrayAdapterHours.setDropDownViewResource(android.R.layout.simple_spinner_item)

            time.adapter = arrayAdapterHours

            time.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedTime = parent.getItemAtPosition(position).toString()

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //Used to handle case for no selection
                    //Not needed for this implementation
                }
            }

//            editText.addTextChangedListener(object : TextWatcher{
//                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                    //Not needed fot this implementation
//                }
//
//                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                }
//
//                override fun afterTextChanged(p0: Editable?) {
//                    classifyTask(editText.text.toString())
//                }
//
//            })

            with(builder){
                setTitle("Add Task")
                setPositiveButton("OK"){dialog ,which->
                    val title = editText.text.toString()


                    val randomNumber = Random.nextLong(10000) //Selects random number up to 99
                    val uniqueID = "$randomNumber"

                    val formBody = FormBody.Builder()
                        .add("id",uniqueID)
                        .add("title",title)
                        .add("tag",selectedTag)
                        .add("hours",selectedTime)
                        .add("username","testUser")
                        .build()

                    val request = Request.Builder()
                        .url("http://192.168.1.112:4998/add_task")
                        .post(formBody)
                        .build()

                    client.newCall(request).enqueue(object : Callback{
                        override fun onFailure(call: Call, e: IOException) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                if (response.isSuccessful) {
                                    requireActivity().runOnUiThread {
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(
                                                requireContext(),"added",Toast.LENGTH_SHORT)
                                                .show()

                                            tasksAdapter.refreshTasks()
                                        }

                                        Toast.makeText(
                                            requireContext(),"added",Toast.LENGTH_SHORT)
                                            .show()


                                    }
                                }
                            } catch (e:Exception) {
                                Toast.makeText(
                                    requireContext(),"Response error",Toast.LENGTH_SHORT)
                                    .show()

                            } finally {
                                response.close()
                            }
                        }

                    })

                }
                setNegativeButton("Cancel"){dialog ,which->
                    Log.d("Tasks","cancelled")
                }
                setView(dialogLayout)
                show()

            }


        }

    }



    //Refresh tasks from backend
    private fun refreshTasksList() {
        val request = Request.Builder().url("http://192.168.1.112:4998/get_tasks").build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(),"Error",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {


                        requireActivity().runOnUiThread{
                            tasksAdapter.refreshTasks()
                        }

                    } else {
                        Toast.makeText(requireContext(),"Failed to fetch tasks",Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception){
                    requireActivity().runOnUiThread{
                        Toast.makeText(requireContext(),"Response error",Toast.LENGTH_SHORT).show()

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

    private fun classifyTask(taskTitle: String) {
        val formBody = FormBody.Builder().add("value",taskTitle).build()

        val request = Request.Builder().url("http://192.168.1.112:4999/classify").post(formBody).build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                //Handle errors
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful){
                    Toast.makeText(requireContext(),"response.body?.toString()",Toast.LENGTH_LONG).show()
                }
                 else {
                Toast.makeText(requireContext(),"not working",Toast.LENGTH_LONG).show()
                }
            }


        })

    }
}