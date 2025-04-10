package com.example.myapplication;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    @Before
    public void setup() {
        ActivityScenario.launch(HomeActivity.class);
    }
    

    @Test
    public void testNavigation() {
        onView(withId(R.id.home_nav_button)).perform(click());
        onView(withId(R.id.home_nav_button)).check(matches(isDisplayed()));

        onView(withId(R.id.tasks_nav_button)).perform(click());
        onView(withId(R.id.tasks_nav_button)).check(matches(isDisplayed()));

        onView(withId(R.id.timer_nav_button)).perform(click());
        onView(withId(R.id.timer_nav_button)).check(matches(isDisplayed()));

        onView(withId(R.id.leaderboard_nav_button)).perform(click());
        onView(withId(R.id.leaderboard_nav_button)).check(matches(isDisplayed()));
    }

}
