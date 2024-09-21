package com.msd.notebook.view.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.msd.notebook.view.fragments.BoardingFragments

class SwipeAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int): Fragment {
        val boardingFragment: Fragment = BoardingFragments()
        val bundle = Bundle()
        bundle.putInt("pageNumber", position)
        boardingFragment.setArguments(bundle)
        return boardingFragment
    }

    override fun getItemCount(): Int {
        return 2
    }
}
