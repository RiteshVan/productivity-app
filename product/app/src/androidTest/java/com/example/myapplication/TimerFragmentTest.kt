package com.example.myapplication

import android.hardware.Sensor
import android.hardware.SensorEvent
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Before
import org.junit.Test

class TimerFragmentTest {
    private lateinit var scenario: FragmentScenario<TimerFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_MyApplication)
        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun testTitlePresent() {
        onView(withId(R.id.timerTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.timerTitle)).check(matches(withText("Timer")))
    }

    @Test
    fun testBarPresent() {
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
    }

    @Test
    fun testTimerEntryPresent() {
        onView(withId(R.id.edit_text_time)).check(matches(isDisplayed()))
    }

    @Test
    fun testTextBoxTyping() {
        onView(withId(R.id.edit_text_time)).perform(typeText("60"))
        onView(withId(R.id.edit_text_time)).check(matches(withText("60")))
    }

    @Test
    fun testInitialTimer() {
        onView(withId(R.id.text_view_time)).check(matches(withText("00:00:00")))
    }

    @Test
    fun testTextBoxUpdatesTimer() {
        onView(withId(R.id.edit_text_time)).perform(typeText("60"), closeSoftKeyboard())
        onView(withId(R.id.edit_text_time)).check(matches(withText("60")))
        onView(withId(R.id.start_stop_button)).perform(click())
        onView(withId(R.id.text_view_time)).check(matches(withText("00:59:59")))
    }

    @Test
    fun testTimerPause() {
        onView(withId(R.id.edit_text_time)).perform(typeText("60"), closeSoftKeyboard())
        onView(withId(R.id.edit_text_time)).check(matches(withText("60")))
        onView(withId(R.id.start_stop_button)).perform(click())
        onView(withId(R.id.text_view_time)).check(matches(withText("00:59:59")))
        onView(withId(R.id.start_stop_button)).perform(click())
        onView(withId(R.id.text_view_time)).check(matches(withText("00:59:59")))
    }

    @Test
    fun testTimerResetTest() {
        onView(withId(R.id.edit_text_time)).perform(typeText("60"), closeSoftKeyboard())
        onView(withId(R.id.edit_text_time)).check(matches(withText("60")))
        onView(withId(R.id.start_stop_button)).perform(click())
        onView(withId(R.id.text_view_time)).check(matches(withText("00:59:59")))
        onView(withId(R.id.start_stop_button)).perform(click())
        onView(withId(R.id.text_view_time)).check(matches(withText("00:59:59")))
        onView(withId(R.id.reset_button)).perform(click())
        onView(withId(R.id.text_view_time)).check(matches(withText("01:00:00")))
    }

}
