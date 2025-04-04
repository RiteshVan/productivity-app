package com.example.myapplication

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LeaderboardFragmentTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var mockClient: OkHttpClient
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockClient = OkHttpClient()
        mockWebServer.start()
    }

    @Test
    fun testJSONLeaderboardParsing() {
        val mockJsonResponse = """{
            "hours_per_user": {
                "Jon": 8,
                "Tom": 12,
                "Leon": 5
            }
        }"""

        mockWebServer.enqueue(MockResponse().setBody(mockJsonResponse))

        val request = Request.Builder().url(mockWebServer.url("/get_hours_per_user")).build()

        val response: Response = mockClient.newCall(request).execute()



        val jsonObject = gson.fromJson(response.body?.string(),Map::class.java)
        val hours = jsonObject["hours_per_user"] as Map<String,Double>

        var leaderboard= hours.map { Pair(it.key,it.value.toInt()) }

        leaderboard=leaderboard.sortedByDescending { it.second }

        val expectedLeaderboard =
            listOf(
                Pair("Tom", 12),
                Pair("Jon", 8),
                Pair("Leon", 5),
            )

        assertEquals(expectedLeaderboard, leaderboard)
    }
}
