package com.example.geozilla

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.geozilla.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_people -> {
                    replaceFragment(PeopleFragment())
                    true
                }
                R.id.bottom_places -> {
                    replaceFragment(PlaceFragment())
                    true
                }
                R.id.bottom_Notifications -> {
                    replaceFragment(NotificationFragment())
                    true
                }
                R.id.bottom_menu -> {
                    replaceFragment(MenuFragment())
                    true
                }
                else -> false
            }
        }

        // Show the DevicesFragment when MainActivity is opened
        replaceFragment(PeopleFragment())

    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
