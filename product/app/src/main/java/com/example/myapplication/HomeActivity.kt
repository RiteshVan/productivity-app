package com.example.myapplication

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.myapplication.databinding.ActivityHomeBinding
import com.google.android.material.navigation.NavigationView

// Main screen of the app with navigation to move to different feature pages
class HomeActivity :
    AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityHomeBinding
    private var username: String? = null // Gets the username from the sign in page

    // Activity is initialised
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Username is retrieved
        username = intent.getStringExtra("username")

        // Listener to identify which item is selected by user
        binding.navigationDrawer.setNavigationItemSelectedListener(this)
        binding.bottomNav.background = null

        // Directs user to chosen fragment
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_nav_button -> startHomeFragment()

                R.id.timer_nav_button -> startFragment(TimerFragment())
                R.id.tasks_nav_button -> startTaskFragment()
                R.id.diary -> startImageDiary()
                R.id.leaderboard_nav_button -> startFragment(LeaderboardFragment())
            }
            true
        }
        fragmentManager = supportFragmentManager

        // By default opens HomeFragment
        startHomeFragment()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not required")
    }

    /**
     * Function used to contain the process of switching features so that code can be
     * more easy to read and simpler to implement
     *
     * @param fragment The fragment that will be displayed
     */
    private fun startFragment(fragment: Fragment) {
        val bundle = Bundle()
        bundle.putString("username", username)
        fragment.arguments = bundle

        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    /**
     * This function is used to open the tasks fragment.
     * Username is passed so that tasks shown are relevant to the user
     */
    private fun startTaskFragment() {
        val tasksFragment = TasksFragment()

        tasksFragment.arguments =
            Bundle().apply {
                putString("username", username)
            }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, tasksFragment)
            .commit()
    }

    /**
     * This function is used to open the home fragment.
     * Username is passed so that chat shown is relevant to user
     */
    private fun startHomeFragment() {
        val homeFragment = HomeFragment()

        homeFragment.arguments =
            Bundle().apply {
                putString("username", username)
            }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()
    }

    /**
     * This function is used to open the image diary fragment.
     * Username is passed so that images shown are relevant to user
     */
    private fun startImageDiary() {
        val imageDiaryFragment = ImageDiaryFragment()

        imageDiaryFragment.arguments =
            Bundle().apply {
                putString("username", username)
            }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, imageDiaryFragment)
            .commit()
    }
}
