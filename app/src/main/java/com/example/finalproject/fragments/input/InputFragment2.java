package com.example.finalproject.fragments.input;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.fragments.MapFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InputFragment2 extends Fragment {
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

    /**
     * After validation occurred, the info given by the user needs to be given to the activity which
     * needs it. This class provides a convenient way for the info to pass anywhere.
     * Pay attention that although any class can READ the data that is saved, no class can create an
     * InputFragment2.PackagedInfo object, except the InputFragment2 class.
     */
    public static class PackagedInfo {
        public final String PHONE;
        public final String COUNTRY;
        public final String CITY;
        public final String ADDRESS;

        private PackagedInfo(String phone, String country, String city, String address) {
            PHONE = phone;
            COUNTRY = country;
            CITY = city;
            ADDRESS = address;
        }
    }

    public InputFragment2(@Nullable User connectedUser) {
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
        final View parent = inflater.inflate(R.layout.fragment_input_2, container, false);

        // Load the phone input views:
        this.tilPhone = parent.findViewById(R.id.fragInput2TilPhoneNumber);
        this.etPhone = parent.findViewById(R.id.fragInput2EtPhoneNumber);
        this.tilCountryCode = parent.findViewById(R.id.fragInput2TilCountryCode);
        this.etCountryCode = parent.findViewById(R.id.fragInput2EtCountryCode);

        // Create a map fragment:
        this.mapFragment = new MapFragment();

        // Put the map fragment in the container:
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragInput2MapFragContainer, this.mapFragment);
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


    public boolean areInputsValid() {
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


    /**
     * Collects the info given by the user in this fragment and packages it for convenient use.
     * Pay attention this function should be called after calling the 'areInputsValid' function,
     * and making sure it returns true.
     * @return An InputFragment2.PackagedInfo object for convenient use of the info given by the
     *         user in this fragment.
     */
    public InputFragment2.PackagedInfo getPackagedInfo() {
        return new PackagedInfo(
                this.mapFragment.getFullNumberWithPlus(),
                this.mapFragment.getSelectedCountry(),
                this.mapFragment.getSelectedCity(),
                this.mapFragment.getSelectedAddress()
        );
    }
}
