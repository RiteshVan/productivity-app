package com.example.myapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.databinding.ActivityHomeBinding
import com.google.android.material.navigation.NavigationView

//Main screen of the app with navigation to move to different feature pages
class HomeActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityHomeBinding



    //Activity is initialised
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Listener to identify which item is selected by user
        binding.navigationDrawer.setNavigationItemSelectedListener(this)
        binding.bottomNav.background=null


        //Directs user to chosen fragment
        binding.bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.home_nav_button->startHomeFragment()
                R.id.timer_nav_button-> startFragment(TimerFragment())
                R.id.setting_nav_button -> startFragment(TagsFragment())
                R.id.tasks_nav_button -> startTaskFragment()
                R.id.leaderboard_nav_button -> startFragment(LeaderboardFragment())
            }
            true
        }
        fragmentManager = supportFragmentManager


        //By default opens HomeFragment
        startHomeFragment()


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }



    //Function used to contain the process of switching features so that code can be more easy to read and simpler to implement
    private fun startFragment(fragment: Fragment){
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container,fragment)
        fragmentTransaction.commit()

    }

    private fun startTaskFragment(){
        val tasksFragment = TasksFragment()


        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,tasksFragment)
            .commit()



    }


    private fun startHomeFragment() {
        val homeFragment = HomeFragment()


        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,homeFragment)
            .commit()


    }

}