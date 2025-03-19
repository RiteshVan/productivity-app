package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class TagsFragment : Fragment() {
    private lateinit var text: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tags, container, false)

        text = view.findViewById(R.id.testing)

        val inputText = view.findViewById<EditText>(R.id.tag_input)

        val okHttpClient = OkHttpClient()

        val cfButton = view.findViewById<Button>(R.id.cf_button)

        cfButton.setOnClickListener {
            val formBody: RequestBody = FormBody.Builder().add("value", inputText.text.toString()).build()

            val request =
                Request
                    .Builder()
                    .url("http://192.168.1.25:5000/classify")
                    .post(formBody)
                    .build()

            okHttpClient.newCall(request).enqueue(
                object : Callback {
                    override fun onFailure(
                        call: Call,
                        e: IOException,
                    ) {
                        requireActivity().runOnUiThread {
                            text.text = "Not working"
                        }
                    }

                    override fun onResponse(
                        call: Call,
                        response: Response,
                    ) {
                        try {
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string()
                                requireActivity().runOnUiThread {
                                    text.text = responseBody ?: "Empty response body"
                                }
                            } else {
                                requireActivity().runOnUiThread {
                                    text.text = "Error: ${response.code}"
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("NetworkError", "Error reading response: ${e.message}")
                            requireActivity().runOnUiThread {
                                text.text = "Response error"
                            }
                        } finally {
                            response.close()
                        }
                    }
                },
            )
        }

        return view
    }
}
