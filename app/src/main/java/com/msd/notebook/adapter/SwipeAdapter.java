package com.msd.notebook.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.msd.notebook.fragments.BoardingFragments;

public class SwipeAdapter extends FragmentStateAdapter {

    public SwipeAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment boardingFragment = new BoardingFragments();
        Bundle bundle = new Bundle();
        bundle.putInt("pageNumber", position);
        boardingFragment.setArguments(bundle);
        return boardingFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
