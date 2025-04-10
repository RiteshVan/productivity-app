package com.example.myapplication

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LeaderboardFragmentTest {
    private lateinit var scenario: FragmentScenario<LeaderboardFragment>
    private lateinit var mockClient: OkHttpClient
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_MyApplication)
        scenario.moveToState(Lifecycle.State.STARTED)

        mockClient = OkHttpClient()

        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @Test
    fun testRecyclerViewIsPresent() {
        onView(withId(R.id.leaderboard_view)).check(matches(isDisplayed()))
    }

    @Test
    fun testLeaderboardAdapterInitialization() {
        scenario.onFragment { fragment ->

            assertNotNull(fragment.leaderboardAdapter)

            assertTrue(fragment.leaderboardAdapter.itemCount == 0)
        }
    }
}
