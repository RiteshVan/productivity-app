package com.example.myapplication

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TasksFragmentTest {


    @Before
    fun setup() {
        ActivityScenario.launch(HomeActivity::class.java)
    }


    @Test
    fun testFragmentDisplayed() {
        onView(withId(R.id.home_nav_button)).perform(click())
        onView(withId(R.id.home_nav_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}