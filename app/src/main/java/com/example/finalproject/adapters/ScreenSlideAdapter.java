package com.example.finalproject.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ScreenSlideAdapter extends FragmentStateAdapter {
    // The fragments inside the main activity:
    private final List<Fragment> fragments;

    public ScreenSlideAdapter(@NonNull FragmentActivity fragmentActivity, Fragment ... fragments) {
        super(fragmentActivity);
        this.fragments = new ArrayList<>(Arrays.asList(fragments));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return this.fragments.size();
    }
}
