package com.example.myapplication

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
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksFragmentTest {
    private lateinit var scenario: FragmentScenario<TasksFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_MyApplication)
        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun testTitleDisplayed() {
        onView(withId(R.id.tasksTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.tasksTitle)).check(matches(withText("Tasks")))
    }

    @Test
    fun testAddTaskButtonOpensAlertDialog() {
        onView(withId(R.id.taskAdd)).perform(click())
        onView(withId(R.id.new_task_add_dialog)).check(matches(isDisplayed()))
    }

    @Test
    fun testTextBoxPresent() {
        onView(withId(R.id.taskAdd)).perform(click())
        onView(withId(R.id.new_task_add_dialog)).check(matches(isDisplayed()))
        onView(withId(R.id.task_add_text)).check(matches(isDisplayed()))
    }

    @Test
    fun testTextTypingInAlertDialog() {
        onView(withId(R.id.taskAdd)).perform(click())
        onView(withId(R.id.new_task_add_dialog)).check(matches(isDisplayed()))
        onView(withId(R.id.task_add_text)).check(matches(isDisplayed()))
        val text = "Testing"
        onView(withId(R.id.task_add_text)).perform(typeText(text), closeSoftKeyboard())
    }

    @Test
    fun testTagSpinnerOpens() {
        onView(withId(R.id.taskAdd)).perform(click())
        onView(withId(R.id.new_task_add_dialog)).check(matches(isDisplayed()))
        onView(withId(R.id.select_tag)).check(matches(isDisplayed()))
        onView(withId(R.id.select_tag)).perform(click())
    }

    @Test
    fun testHoursSpinnerOpens() {
        onView(withId(R.id.taskAdd)).perform(click())
        onView(withId(R.id.new_task_add_dialog)).check(matches(isDisplayed()))
        onView(withId(R.id.select_hours)).check(matches(isDisplayed()))
        onView(withId(R.id.select_hours)).perform(click())
    }

    @Test
    fun testRecyclerViewIsPresent() {
        onView(withId(R.id.tasksView)).check(matches(isDisplayed()))
    }
}
