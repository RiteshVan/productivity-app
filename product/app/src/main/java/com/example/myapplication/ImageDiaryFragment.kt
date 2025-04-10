package com.example.myapplication

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Job
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


/**
 * This fragment is used to display images of tasks users have completed along with their captions
 */
class ImageDiaryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private val imageList = mutableListOf<ImageItem>()

    private var username: String? = null

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

        // Gets username
        arguments.let {
            username = it?.getString("username")
        }

        recyclerView = view.findViewById(R.id.imageView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        imageAdapter = ImageAdapter(imageList, requireContext())
        recyclerView.adapter = imageAdapter

        return view
    }

    // Images and captions are obtained from backend
    private fun getImages() {
        val client = OkHttpClient()

        val request = Request.Builder().url("http://192.168.1.112:4997/get_images/$username").build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    // Message shown if cannot get images from backend
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Get images failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    val response = response.body?.string()

                    val images = parseImages(response)

                    //Used to update recycler view to show images
                    requireActivity().runOnUiThread {
                        imageAdapter.updateData(images)
                    }
                }
            },
        )
    }

    /**
     * Image details are obtained and list of image items is created
     *
     * @param responseBody The JSON response that contains the data
     * @return a list of image items is returned
     */
    fun parseImages(responseBody: String?): List<ImageItem> {
        // Mutable list is used so images can be added to the list
        val images = mutableListOf<ImageItem>()

        if (responseBody != null) {
            try {
                val json = JSONObject(responseBody)
                val imageArray: JSONArray = json.getJSONArray("images")

                //For each item in json response, details are extracted and used to form the objects
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
                e.printStackTrace()
            }
        }
        // List is returned
        return images
    }
}
