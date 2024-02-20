package com.example.finalproject.fragments.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalproject.R;

public class ShiftsFragment extends Fragment {
    public ShiftsFragment() {
        // Required empty public constructor
    }

    public static ShiftsFragment newInstance() {
        return new ShiftsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_shifts, container, false);
    }
}