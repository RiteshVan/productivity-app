package com.example.myapplication

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Test

class ImageDiaryFragmentTest {

    private lateinit var scenario: FragmentScenario<ImageDiaryFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_MyApplication)
        scenario.moveToState(Lifecycle.State.STARTED)


    }

    @Test
    fun testRecyclerViewIsPresent() {
        onView(withId(R.id.imageView)).check(matches(isDisplayed()))
    }


    @Test
    fun testRecyclerViewHasNoItems(){
        onView(withId(R.id.imageView)).check(matches(hasChildCount(0)))
    }
}