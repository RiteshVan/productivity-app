package com.example.myapplication

import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.eazegraph.lib.charts.PieChart
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {
    private var calendar: Calendar? = null

    private lateinit var scenario: FragmentScenario<HomeFragment>



    private var homeFragment: HomeFragment? = null

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_MyApplication)
        scenario.moveToState(Lifecycle.State.STARTED)

        val mockWebServer = MockWebServer()
        mockWebServer.start()
        MockitoAnnotations.openMocks(this)

        calendar = Calendar.getInstance()

        homeFragment = HomeFragment()



        homeFragment?.client = OkHttpClient()
    }

    @Test
    fun testMorningGreeting() {
        calendar!![Calendar.HOUR_OF_DAY] = 9

        val greeting = homeFragment!!.setGreeting(calendar!!)

        assertEquals("Good Morning!", greeting)
    }

    @Test
    fun testAfternoonGreeting() {
        calendar!![Calendar.HOUR_OF_DAY] = 13

        val greeting = homeFragment!!.setGreeting(calendar!!)

        assertEquals("Good Afternoon!", greeting)
    }

    @Test
    fun testEveningGreeting() {
        calendar!![Calendar.HOUR_OF_DAY] = 19

        val greeting = homeFragment!!.setGreeting(calendar!!)

        assertEquals("Good Evening!", greeting)
    }

    @Test
    fun testPieChartIsDisplayed() {
        onView(withId(R.id.pie_chart)).check(matches(isDisplayed()))
    }

    @Test
    fun testPieChartUpdate() {
        scenario.onFragment { fragment ->
            val chart = fragment.view?.findViewById<PieChart>(R.id.pie_chart)

            assertNotNull(chart)

            fragment.updatePieChart(
                8f,
                2f,
                3f,
                4f,
                5f,
                6f,
            )

            assertEquals(6, chart?.data?.size)
            assertEquals(8f, chart?.data?.get(0)?.value)
            assertEquals(2f, chart?.data?.get(1)?.value)
            assertEquals(3f, chart?.data?.get(2)?.value)
            assertEquals(4f, chart?.data?.get(3)?.value)
            assertEquals(5f, chart?.data?.get(4)?.value)
            assertEquals(6f, chart?.data?.get(5)?.value)
        }
    }






}
