package com.msd.notebook.view.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.msd.notebook.R
import com.msd.notebook.common.Constants.LOGGED_IN_AS
import com.msd.notebook.common.Constants.STUDENT
import com.msd.notebook.common.PreferenceClass
import com.msd.notebook.databinding.ActivityHomeBinding
import com.msd.notebook.view.fragments.AnnouncementsFragment
import com.msd.notebook.view.fragments.InstructorFilesFragment
import com.msd.notebook.view.fragments.LecturesFragment
import com.msd.notebook.view.fragments.QrCodeDisplayFrag

class HomeActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    var binding: ActivityHomeBinding? = null
    lateinit var preferenceClass: PreferenceClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceClass = PreferenceClass(this@HomeActivity)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        binding!!.bottomNavigation
            .setOnNavigationItemSelectedListener(this@HomeActivity)
        binding!!.bottomNavigation.selectedItemId = R.id.navigation_files

        if(preferenceClass.getString(LOGGED_IN_AS).equals(STUDENT)){
            binding!!.bottomNavigation.menu.findItem(R.id.navigation_qr).isVisible = false
        }
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
                .replace(R.id.my_nav_host_fragment, InstructorFilesFragment())
                .commit()
            return true
        } else if (item.itemId == R.id.navigation_announcements) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.my_nav_host_fragment, AnnouncementsFragment())
                .commit()
            return true
        } else if (item.itemId == R.id.navigation_lectures) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.my_nav_host_fragment, LecturesFragment())
                .commit()
            return true
        }
        return false
    }
}