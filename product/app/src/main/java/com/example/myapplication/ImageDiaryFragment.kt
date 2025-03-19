package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ImageDiaryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private val imageList = mutableListOf<ImageItem>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        getImages()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_diary, container, false)

        recyclerView = view.findViewById(R.id.imageView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        imageAdapter = ImageAdapter(imageList, requireContext())
        recyclerView.adapter = imageAdapter

        return view
    }

    private fun getImages() {
        val client = OkHttpClient()

        val request = Request.Builder().url("http://192.168.1.112:4997/get_images").build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    e.printStackTrace()
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    response.body?.string()?.let { responseBody ->
                        val images = parseImages(responseBody)
                        requireActivity().runOnUiThread {
                            imageAdapter.updateData(images)
                        }
                    }
                }
            },
        )
    }

    fun parseImages(responseBody: String?): List<ImageItem> {
        val images = mutableListOf<ImageItem>()

        if (responseBody != null) {
            try {
                val json = JSONObject(responseBody)
                val imageArray: JSONArray = json.getJSONArray("images")

                for (i in 0 until imageArray.length()) {
                    val image = imageArray.getJSONObject(i)
                    val imageItem =
                        ImageItem(
                            imageUrl = image.getString("image"),
                            caption = image.getString("caption"),
                        )
                    images.add(imageItem)
                }
            } catch (e: Exception) {
                Log.d("Image Diary Parse Error", e.message.toString())
            }
        }

        return images
    }
}
