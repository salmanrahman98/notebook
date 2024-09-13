package com.msd.notebook.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.msd.notebook.adapter.SwipeAdapter;
import com.msd.notebook.databinding.ActivityBoardingScreensBinding;

public class BoardingScreens extends AppCompatActivity {

    public SwipeAdapter swipeAdapter;
    public int post = 0;
    ActivityBoardingScreensBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBoardingScreensBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        swipeAdapter = new SwipeAdapter(getSupportFragmentManager(), this.getLifecycle());
        binding.viewPager.setAdapter(swipeAdapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                post = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }
}