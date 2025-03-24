package com.example.myapplication;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;

import org.mockito.MockitoAnnotations;



import android.icu.util.Calendar;


import java.lang.reflect.Field;


import okhttp3.OkHttpClient;



@RunWith(AndroidJUnit4.class)
public class HomeFragmentTest {

    private Calendar calendar;


    private HomeFragment homeFragment;






    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        calendar = Calendar.getInstance();


        homeFragment = new HomeFragment();
    }


    @Test
    public void testMorningGreeting() {
        calendar.set(Calendar.HOUR_OF_DAY,9);

        String greeting = homeFragment.setGreeting(calendar);

        assertEquals("Good Morning!",greeting);

    }

    @Test
    public void testAfternoonGreeting() {
        calendar.set(Calendar.HOUR_OF_DAY,13);

        String greeting = homeFragment.setGreeting(calendar);

        assertEquals("Good Afternoon!",greeting);

    }

    @Test
    public void testEveningGreeting() {
        calendar.set(Calendar.HOUR_OF_DAY,19);

        String greeting = homeFragment.setGreeting(calendar);

        assertEquals("Good Evening!",greeting);

    }


    @Test
    public void testPieChartDisplay() {


    }









}
