package com.msd.notebook.view.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.msd.notebook.R
import com.msd.notebook.databinding.ActivityHomeBinding
import com.msd.notebook.view.fragments.InstructorHomeFragment
import com.msd.notebook.view.fragments.QrCodeDisplayFrag
import com.msd.notebook.view.fragments.SettingsFragment

class HomeActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        binding!!.bottomNavigation
            .setOnNavigationItemSelectedListener(this@HomeActivity)
        binding!!.bottomNavigation.selectedItemId = R.id.bottom_navigation
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.navigation_qr) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.my_nav_host_fragment, QrCodeDisplayFrag())
                .commit()
            return true
        } else if (item.itemId == R.id.navigation_files) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.my_nav_host_fragment, InstructorHomeFragment())
                .commit()
            return true
        } else if (item.itemId == R.id.navigation_announcements) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.my_nav_host_fragment, SettingsFragment())
                .commit()
            return true
        }
        return false
    }
}