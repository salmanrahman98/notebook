package com.msd.notebook.view.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.msd.notebook.view.adapter.SwipeAdapter
import com.msd.notebook.databinding.ActivityBoardingScreensBinding

class BoardingScreens : AppCompatActivity() {
    var swipeAdapter: SwipeAdapter? = null
    var post = 0
    var binding: ActivityBoardingScreensBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardingScreensBinding.inflate(layoutInflater)
        val view: View = binding!!.getRoot()
        setContentView(view)
        swipeAdapter = SwipeAdapter(supportFragmentManager, this.lifecycle)
        binding!!.viewPager.setAdapter(swipeAdapter)
        binding!!.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                post = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }
}