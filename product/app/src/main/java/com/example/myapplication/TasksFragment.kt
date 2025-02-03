package com.example.myapplication

import android.app.Fragment
import android.os.Bundle
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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentTasksBinding
import com.google.android.material.color.utilities.ToneDeltaPair


class TasksFragment : Fragment() {

    private  var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var usernameText: String
    private lateinit var db:TaskDatabase
    private lateinit var tasksAdapter:TasksAdapter

    private lateinit var selectedTag:String
    private lateinit var selectedTime:String




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentTasksBinding.inflate(inflater,container,false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = TaskDatabase(requireContext())
        tasksAdapter= TasksAdapter(db.getTasks(),requireContext(),usernameText)

        val tasksView =binding.tasksView
        tasksView.layoutManager = LinearLayoutManager(requireContext())
        tasksView.adapter=tasksAdapter

        editTaskDialog()



    }

    override fun onResume() {
        super.onResume()
        tasksAdapter.refreshData(db.getTasks())
    }


    //Dialog popup to add task details
    //Keeps log of tasks added
    private fun editTaskDialog(){
        binding.taskAdd.setOnClickListener(){
            db = TaskDatabase(requireContext())

            val builder =AlertDialog.Builder(requireContext())
            val inflater = layoutInflater
            val dialogLayout =inflater.inflate(R.layout.new_task_add_dialog,null)
            val editText = dialogLayout.findViewById<EditText>(R.id.task_add_text)
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

                }

            }


            val time =  dialogLayout.findViewById<Spinner>(R.id.select_hours)

            val listHours = listOf("1","2","3","4","5")

            val arrayAdapterHours = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,listHours)
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)

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

                }

            }



            with(builder){
                setTitle("Add Task")
                setPositiveButton("OK"){dialog ,which->
                    tasksAdapter.refreshData(db.getTasks())
                    Log.d("Tasks","editText.text.toString()")
                    val title = editText.text.toString()
                    db.addTask(Task(title,selectedTag,selectedTime.toInt()))
                    tasksAdapter.refreshData(db.getTasks())
                }
                setNegativeButton("Cancel"){dialog ,which->
                    Log.d("Tasks","cancelled")
                }
                setView(dialogLayout)
                show()

            }


        }

    }

}