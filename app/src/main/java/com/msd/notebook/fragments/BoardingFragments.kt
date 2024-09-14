package com.msd.notebook.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.msd.notebook.R
import com.msd.notebook.activity.LoginActivity

class BoardingFragments : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View
        val bundle = arguments
        val pageNumber = bundle!!.getInt("pageNumber")
        return when (pageNumber) {
            0 -> {
                view = inflater.inflate(R.layout.boarding_screen1, container, false)
                view.findViewById<View>(R.id.next_card).setOnClickListener { view1: View? ->
                    val viewpager = activity!!.findViewById<ViewPager2>(R.id.viewPager)
                    viewpager.setCurrentItem(viewpager.currentItem + 1, true)
                }
                view
            }

            1 -> {
                view = inflater.inflate(R.layout.boarding_screen2, container, false)
                view.findViewById<View>(R.id.next_card).setOnClickListener {
                    val intent = Intent(activity, LoginActivity::class.java)
                    startActivity(intent)
                }
                view
            }

            else -> {
                view = inflater.inflate(R.layout.activity_login, container, false)
                view
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
