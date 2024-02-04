package com.example.finalproject.fragments.input.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.finalproject.R;
import com.example.finalproject.fragments.MapFragment;
import com.example.finalproject.fragments.input.InputFragment;

public class BusinessInputFragment2 extends InputFragment {
    // The country of the branch:
    private final String country;

    // The map fragment allowing the user to choose their branch's address:
    private MapFragment mapFragment;

    public BusinessInputFragment2(String country) {
        this.country = country;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the XML file:
        final View parent = inflater.inflate(R.layout.fragment_business_input_2, container, false);

        // Create the map fragment (if it hadn't already been created):
        if (this.mapFragment == null)
            this.mapFragment = new MapFragment();

        // TODO: Restrict the map borders based on the country

        return parent;
    }

    @Override
    public boolean validateAndSetError() {
        return false;
    }

    @Override
    public Bundle getInputs() {
        return null;
    }
}
