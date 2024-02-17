package com.example.finalproject.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ScreenSlideAdapter extends FragmentStateAdapter {
    // The fragments inside the main activity:
    private final Fragment[] fragments;

    public ScreenSlideAdapter(@NonNull FragmentActivity fragmentActivity, Fragment[] fragments) {
        super(fragmentActivity);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return this.fragments[position];
    }

    @Override
    public int getItemCount() {
        return this.fragments.length;
    }
}
