package com.example.finalproject.fragments.input;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.InputValidation;
import com.example.finalproject.database.entities.User;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.function.Function;

public class InputFragment2 extends Fragment {
    // The views that show the country:
    private TextInputLayout tilCountry;
    private TextInputEditText etCountry;

    // The views that show the city:
    private TextInputLayout tilCity;
    private TextInputEditText etCity;

    // The views that show the address:
    private TextInputLayout tilAddress;
    private TextInputEditText etAddress;

    // The selected country from the previous fragment:
    private final String countryName;

    // The callback that will receive the location:
    private ActivityResultLauncher<Intent> locationReceiver;

    /**
     * After validation occurred, the info given by the user needs to be given to the activity which
     * needs it. This class provides a convenient way for the info to pass anywhere.
     * Pay attention that although any class can READ the data that is saved, no class can create an
     * InputFragment2.PackagedInfo object, except the InputFragment2 class.
     */
    public static class PackagedInfo {
        public final String CITY;
        public final String ADDRESS;

        private PackagedInfo(String city, String address) {
            CITY = city;
            ADDRESS = address;
        }
    }

    public InputFragment2(String countryName) {
        this.countryName = countryName;
    }

    public void loadInputsFromUser(User user) {
        // TODO: When switching to Firestore this function will have to be changed
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the second input fragment:
        final View parent = inflater.inflate(R.layout.fragment_input_2, container, false);
        // TODO: Add the map view and the API key

        return parent;
    }

    public boolean areInputsValid() {
        return true;
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
                this.getCity(),
                this.getAddress()
        );
    }

    private String getAddress() {
        // TODO: Complete the function to return the address after the map view is finished
        return "";
    }

    private String getCity() {
        // TODO: Complete the function to return the city after the map view is finished
        return "";
    }
}
