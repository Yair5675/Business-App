package com.example.finalproject.fragments.input.business;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

        // Restrict the map borders based on the country
        this.mapFragment.restrictToCountry(this.country);

        return parent;
    }

    @Override
    public boolean validateAndSetError() {
        // Get the location:
        final String selectedCountry = this.mapFragment.getSelectedCountry(),
                selectedCity = this.mapFragment.getSelectedCity(),
                selectedAddress = this.mapFragment.getSelectedAddress();

        // Check the location:
        final boolean fullLocation = selectedCountry != null && selectedCity != null && selectedAddress != null;

        if (!fullLocation) {
            Toast.makeText(requireContext(), "Please choose your address", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check that the selected country is the received country:
        if (!selectedCountry.equals(this.country)) {
            Toast.makeText(requireContext(), "Country must be " + this.country, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public Bundle getInputs() {
        // Create an empty bundle:
        final Bundle bundle = new Bundle();

        // Save the address there:
        bundle.putString("selectedCountry", this.country);
        bundle.putString("selectedCity", this.mapFragment.getSelectedCity());
        bundle.putString("selectedAddress", this.mapFragment.getSelectedAddress());

        // Return the bundle:
        return bundle;
    }
}
