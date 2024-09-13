package com.msd.notebook.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.msd.notebook.R;
import com.msd.notebook.databinding.ActivityHomeBinding;
import com.msd.notebook.fragments.InstructorHomeFragment;
import com.msd.notebook.fragments.QrCodeDisplayFrag;
import com.msd.notebook.fragments.SettingsFragment;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView
        .OnNavigationItemSelectedListener {
    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.bottomNavigation
                .setOnNavigationItemSelectedListener(HomeActivity.this);
        binding.bottomNavigation.setSelectedItemId(R.id.bottom_navigation);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.navigation_qr) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.my_nav_host_fragment, new QrCodeDisplayFrag())
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.navigation_home) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.my_nav_host_fragment, new InstructorHomeFragment())
                    .commit();
            return true;
        } else if (item.getItemId() == R.id.navigation_settings) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.my_nav_host_fragment, new SettingsFragment())
                    .commit();
            return true;
        }
        return false;
    }
}