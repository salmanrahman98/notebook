package com.msd.notebook.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.msd.notebook.R;
import com.msd.notebook.activity.LoginActivity;

import org.jetbrains.annotations.NotNull;

public class BoardingFragments extends Fragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view;
        Bundle bundle = getArguments();
        int pageNumber = bundle.getInt("pageNumber");


        switch (pageNumber) {
            case 0:
                view = inflater.inflate(R.layout.boarding_screen1, container, false);

                view.findViewById(R.id.next_card).setOnClickListener(view1 -> {
                    ViewPager2 viewpager = getActivity().findViewById(R.id.viewPager);
                    viewpager.setCurrentItem(viewpager.getCurrentItem() + 1, true);
                });
                return view;

            case 1:
                view = inflater.inflate(R.layout.boarding_screen2, container, false);
                view.findViewById(R.id.next_card).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
                return view;
            default:
                view = inflater.inflate(R.layout.activity_login, container, false);
                return view;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}
