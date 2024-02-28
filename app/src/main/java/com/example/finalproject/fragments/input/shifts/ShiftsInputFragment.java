package com.example.finalproject.fragments.input.shifts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalproject.R;
import com.example.finalproject.fragments.input.InputFragment;

public class ShiftsInputFragment extends InputFragment {
    public ShiftsInputFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the XML file:
        final View parent = inflater.inflate(R.layout.fragment_shifts_input, container, false);

        // TODO: Load views

        return parent;
    }

    @Override
    public boolean validateAndSetError() {
        // TODO: Validate shifts
        return false;
    }

    @Override
    public Bundle getInputs() {
        // TODO: Return a bundle with a shift and a list of workers
        return null;
    }
}
