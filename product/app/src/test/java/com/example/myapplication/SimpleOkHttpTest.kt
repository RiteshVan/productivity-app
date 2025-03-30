package com.example.myapplication


import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals

import org.junit.Before
import org.junit.Test

class SimpleOkHttpTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var mockClient:OkHttpClient
    private val gson = Gson()


    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        mockClient = OkHttpClient()

    }


    @Test
    fun testRequest() {
        mockWebServer.enqueue(MockResponse().setBody("Test passed"))

        val request = Request.Builder().url(mockWebServer.url("/test")).build()

        val response:Response = mockClient.newCall(request).execute()

        assertEquals(200,response.code)
        assertEquals("Test passed",response.body?.string())

    }


    @Test
    fun testJSONResponse() {
        val mockJsonResponse = """{
            "id": 99,
            "title":"Go for a run",
            "tag":"Work",
            "hours":5,
            "username": "Jon Jones"
        }"""

        mockWebServer.enqueue(MockResponse().setBody(mockJsonResponse))

        val request =Request.Builder().url(mockWebServer.url("/task")).build()

        val response:Response = mockClient.newCall(request).execute()

        val task = gson.fromJson(response.body?.string(), Task::class.java)

        assertEquals(99,task.id)

    }

    @After
    fun teardown(){
        mockWebServer.shutdown()
    }

}