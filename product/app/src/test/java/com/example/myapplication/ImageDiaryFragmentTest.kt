package com.example.myapplication

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ImageDiaryFragmentTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var mockClient: OkHttpClient
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockClient = OkHttpClient()
    }

    @Test
    fun testJSONResponse() {
        val mockJsonResponse = """
            {
            
            "image": "image_url",
            "caption": "Caption"
                   
                    
            }
        """

        mockWebServer.enqueue(MockResponse().setBody(mockJsonResponse))

        val request = Request.Builder().url(mockWebServer.url("/get_images")).build()

        val response: Response = mockClient.newCall(request).execute()

        val image = gson.fromJson(response.body?.string(), ImageItem::class.java)

        assertEquals("Caption",image.caption)
    }
}
