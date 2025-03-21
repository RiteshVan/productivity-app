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

class TasksFragmentTest {

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
    fun testJSONResponse() {

        val mockJsonResponse = """{
           "id": 99,
           "title":"Go for a run",
           "tag":"Work",
           "hours":5,
           "username": "Jon"
       }"""

        mockWebServer.enqueue(MockResponse().setBody(mockJsonResponse))

        val request =Request.Builder().url(mockWebServer.url("/task")).build()

        val response:Response = mockClient.newCall(request).execute()

        val task = gson.fromJson(response.body?.string(), Task::class.java)

        assertEquals(99,task.id)
        assertEquals("Go for a run",task.title)
        assertEquals("Work",task.tag)
        assertEquals(5,task.hours)
        assertEquals("Jon",task.username)
    }

    @After
    fun teardown(){
        mockWebServer.shutdown()
    }


}