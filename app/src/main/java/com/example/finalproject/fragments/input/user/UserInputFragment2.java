package com.example.finalproject.fragments.input.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.MapFragment;

import com.example.finalproject.fragments.input.InputFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class UserInputFragment2 extends InputFragment {
    // A reference to the user whose details are being changed:
    private final User user;

    // The map fragment itself:
    private MapFragment mapFragment;

    // The country code picker that receives the user's phone number and country:
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;

    // The edit text that displays the country code digits selected:
    private TextInputLayout tilCountryCode;
    private TextInputEditText etCountryCode;

    // Keys for the input bundle:
    public static final String PHONE_KEY = "phone";
    public static final String COUNTRY_KEY = "country";
    public static final String CITY_KEY = "city";
    public static final String ADDRESS_KEY = "address";

    public UserInputFragment2(@Nullable User connectedUser) {
        this.user = connectedUser;
    }

    public void loadInputsFromUser(User user) {
        // Check that the inputs are empty:
        final String selectedAddress = this.mapFragment.getSelectedAddress(),
                selectedCity = this.mapFragment.getSelectedCity(),
                selectedCountry = this.mapFragment.getSelectedCountry();
        if (selectedAddress == null && selectedCity == null && selectedCountry == null) {
            // Combine the address, city and country:
            final String location = String.format(
                    "%s, %s, %s", user.getAddress(), user.getCity(), user.getCountry()
            );

            // Use reverse geocoding:
            this.mapFragment.loadFromLocation(location);

            // Set the phone:
            this.mapFragment.setPhoneNumber(user.getPhoneNumber());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the second input fragment:
        final View parent = inflater.inflate(R.layout.fragment_user_input_2, container, false);

        // Load the phone input views:
        this.tilPhone = parent.findViewById(R.id.fragUserInput2TilPhoneNumber);
        this.etPhone = parent.findViewById(R.id.fragUserInput2EtPhoneNumber);
        this.tilCountryCode = parent.findViewById(R.id.fragUserInput2TilCountryCode);
        this.etCountryCode = parent.findViewById(R.id.fragUserInput2EtCountryCode);

        // Create a map fragment:
        if (this.mapFragment == null)
            this.mapFragment = new MapFragment();

        // Put the map fragment in the container:
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragUserInput2MapFragContainer, this.mapFragment);
        transaction.commit();

        // Once the map fragment is created run the following code:
        transaction.runOnCommit(() -> {
            // Bind the phone number input to the fragment:
            this.mapFragment.connectPhoneInput(
                    this.tilCountryCode, this.etCountryCode, this.tilPhone, this.etPhone
            );

            // If there is a user, load the location from them:
            if (this.user != null)
                loadInputsFromUser(this.user);
        });

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

        // Return true if the phone is valid (error message is in the input field):
        return this.mapFragment.isPhoneValid();
    }

    @Override
    public Bundle getInputs() {
        // Create an empty bundle:
        final Bundle inputBundle = new Bundle();

        // Load the inputs:
        inputBundle.putString(PHONE_KEY, this.mapFragment.getFullNumberWithPlus());
        inputBundle.putString(COUNTRY_KEY, this.mapFragment.getSelectedCountry());
        inputBundle.putString(CITY_KEY, this.mapFragment.getSelectedCity());
        inputBundle.putString(ADDRESS_KEY, this.mapFragment.getSelectedAddress());

        // Return the bundle:
        return inputBundle;
    }

}
