package com.example.myapplication;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;
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
    public void testUpdatePieChart() {


        float workHours = 3.0f;
        float personalHours= 4.0f;
        float exerciseHours =5.0f;
        float shoppingHours = 6.0f;
        float uniWorkHours = 8.0f;
        float gardeningHours= 14.0f;

        PieChart mockPieChart = mock(PieChart.class);

        try{
            Field fieldChart = HomeFragment.class.getDeclaredField("pieChart");
            fieldChart.setAccessible(true);
            fieldChart.set(homeFragment,mockPieChart);
        }

        catch (Exception e){
            fail("Error : Could not set the chart");
        }


        homeFragment.updatePieChart(workHours,personalHours,exerciseHours,shoppingHours,uniWorkHours,gardeningHours);

        verify(mockPieChart,times(6)).addPieSlice(any(PieModel.class));

        verify(mockPieChart).animate();
    }







}
