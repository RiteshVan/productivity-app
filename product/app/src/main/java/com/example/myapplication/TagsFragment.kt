package com.example.myapplication

import android.icu.util.Output
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Response


class TagsFragment : Fragment() {


    private lateinit var input: EditText
    private lateinit var output: TextView

    private lateinit var tagButton: Button




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tags, container, false)
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        input = view.findViewById(R.id.tags_input)
        output = view.findViewById(R.id.tags_output)
        tagButton = view.findViewById(R.id.label_button)

        }

    }




    





