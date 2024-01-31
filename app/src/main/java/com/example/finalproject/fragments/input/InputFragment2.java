package com.example.finalproject.fragments.input;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.threads.GeocodingThread;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.GeocodingUtil;
import com.example.finalproject.util.Util.Zoom;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.maps.model.GeocodingResult;
import com.hbb20.CountryCodePicker;

import java.util.Locale;
import java.util.function.Function;

public class InputFragment2 extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    // A reference to the user whose details are being changed:
    private final User user;

    // The map that allows the user to choose a location:
    private GoogleMap map;

    // The view of that map:
    private MapView mapView;

    // The layout containing all info about the selected location, will be shown only after
    // selection on the map:
    private LinearLayout locationLayout;

    // The country code picker that receives the user's phone number and country:
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;
    private CountryCodePicker countryCodePicker;

    // The edit text that displays the country code digits selected:
    private TextInputLayout tilCountryCode;
    private TextInputEditText etCountryCode;

    // The progress bar that will be shown while the reverse geocoding is happening:
    private ProgressBar pbLocationLoader;

    // The edit text holding the picked city:
    private TextInputLayout tilCity;
    private TextInputEditText etCity;

    // The edit text holding the picked address:
    private TextInputLayout tilAddress;
    private TextInputEditText etAddress;

    // The selected country, city and address:
    private String selectedCountry, selectedCity, selectedAddress;

    // The last result from the geocoding API. Is used to load the saved location if the user went
    // back and forth in the fragments:
    private GeocodingResult lastGeoResult;

    // A variable to handle if the google API is available:
    private boolean isMapsApiAvailable;

    // The duration of the zoom animation in milliseconds:
    private static final int ZOOM_DURATION = 1000;

    // The zoom level used if no bounds are returned in the response:
    public static final int DEFAULT_ZOOM_LEVEL = Zoom.STREETS.level;

    // A tag for logging purposes:
    private static final String TAG = "InputFragment2";

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
        if (selectedAddress == null && selectedCity == null && selectedCountry == null) {
            // Combine the address, city and country:
            final String location = String.format(
                    "%s, %s, %s", user.getAddress(), user.getCity(), user.getCountry()
            );

            // Use reverse geocoding:
            this.loadFromLocation(location);

            // Set the phone:
            this.countryCodePicker.setFullNumber(user.getPhoneNumber());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the second input fragment:
        final View parent = inflater.inflate(R.layout.fragment_input_2, container, false);

        // Initialize the edit texts and input layouts for them:
        this.initEditTexts(parent);
        this.initInputLayouts(parent);

        // Re-establish the CCP:
        this.initCountryCodePicker(parent);

        // Initialize the search view and load the onSearchListener:
        this.initSearchView(parent);

        // Load the progress bar and set it to gone:
        this.pbLocationLoader = parent.findViewById(R.id.fragInput2PbLocationLoader);
        this.pbLocationLoader.setVisibility(View.GONE);

        // Load the location layout:
        this.locationLayout = parent.findViewById(R.id.fragInput2LayoutLocationDetails);

        // Hide all the location details:
        this.locationLayout.setVisibility(View.GONE);

        // Initialize the map:
        this.mapView = parent.findViewById(R.id.fragInput2MapView);
        // Check that the service is available:
        this.isMapsApiAvailable = checkGooglePlayServices();
        if (this.isMapsApiAvailable) {
            // Use the onCreate life-cycle method on the map:
            this.mapView.onCreate(savedInstanceState);

            // Add the activity as a callback:
            this.mapView.getMapAsync(this);
        }

        // If a user was given, use them to load their location:
        if (this.user != null)
            this.loadInputsFromUser(this.user);

        return parent;
    }

    private void initSearchView(View parent) {
        // The search view allowing the user to search a location:
        final SearchView svMap = parent.findViewById(R.id.fragInput2MapSearch);

        svMap.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the reverse geocoding:
                Log.d(TAG, "Search button pressed");
                loadFromLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // While the text is changing no geocoding operation is carried out
                return false;
            }
        });
    }

    private void loadFromLocation(String location) {
        if (!isMapsApiAvailable) {
            Toast.makeText(requireContext(), "Google Maps is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the location is empty:
        if (location == null || location.length() == 0) {
            Toast.makeText(
                    requireContext(),
                    "Please enter a location",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Log.d(TAG, "Given address: " + location);

        // Create a handler that accepts the location returned from the thread and sets the
        // map to it:
        final Handler geoHandler = new Handler(
                Looper.getMainLooper(),
                InputFragment2.this::handleGeocoderResult
        );

        // Create the thread and start it:
        final GeocodingThread geoThread = GeocodingThread.getGeocoderThread(
                geoHandler,
                location
        );

        geoThread.start();

        // Hide the location details layout and show the progress bar:
        hideLocationInfo();
    }

    /**
     * Handles a normal geocoding result. This function is meant to be the geocoding thread's
     * callback.
     * @param message The message returned from the geocoding thread containing the searched
     *                address.
     * @return False always to signal the message will need further handling.
     * @noinspection SameReturnValue
     */
    private boolean handleGeocoderResult(@NonNull Message message) {
        // Check that the thread returned a Result<Address, String>:
        if (!(message.obj instanceof Result)) {
            Log.e(
                    TAG,
                    "Unexpected type returned from geocoding thread (expected Result): "
                            + message.obj.getClass().getName()
            );
            return false;
        }

        // Check if the result was a success:
        Result<?, ?> result = (Result<?, ?>) message.obj;
        if (result.isOk()) {
            // Ensure type checking again and get the address:
            if (!(result.getValue() instanceof GeocodingResult)) {
                Log.e(
                        TAG,
                        "Unexpected type for successful type returned from geocoding thread " +
                                "(expected GeocodingResult): " + result.getValue().getClass().getName()
                );
                return false;
            }

            // Load the info from the address and save the result:
            this.lastGeoResult = (GeocodingResult) result.getValue();
            this.loadFromResult(this.lastGeoResult);

        } else {
            // Remove the progress bar and alert the user:
            Log.e(TAG, result.getError().toString());
            this.pbLocationLoader.setVisibility(View.GONE);
            if (result.getError().toString().equals(GeocodingUtil.NO_RESULTS_ERROR))
                Toast.makeText(requireContext(), GeocodingUtil.NO_RESULTS_ERROR, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();

            // Remove the previously saved location:
            this.selectedAddress = null;
            this.selectedCity = null;
            this.selectedCountry = null;

            // Clear previous markers:
            this.map.clear();
        }

        return false;
    }

    private void loadFromResult(GeocodingResult geocodingResult) {
        // Display the result:
        this.displayLocationInfo(geocodingResult);

        // Set the marker on the map and move to it:
        final LatLng coordinates = new LatLng(
                geocodingResult.geometry.location.lat, geocodingResult.geometry.location.lng
        );
        this.setMarkerOnMap(coordinates);

        // Move to the location bounds:
        final Result<LatLngBounds, String> bounds = GeocodingUtil.getBounds(geocodingResult);
        if (bounds.isOk())
            this.moveToLocation(bounds.getValue());
        else {
            Log.e(TAG, bounds.getError());
            this.moveToLocation(coordinates);
        }
    }

    private void displayLocationInfo(GeocodingResult location) {
        // Show the location info layout:
        this.locationLayout.setVisibility(View.VISIBLE);

        // Hide the progress bar:
        this.pbLocationLoader.setVisibility(View.GONE);

        // Saving the invalid toast message in case we'll use it:
        final Toast invalidLocationToast = Toast.makeText(
                requireContext(), "Invalid location", Toast.LENGTH_SHORT
        );

        boolean isAddressValid;

        // Set the country:
        final Result<String, String> countryCodeResult = GeocodingUtil.getCountryCode(location);
        if (countryCodeResult.isOk())
            this.setCountry(countryCodeResult.getValue());
        else {
            // If the address isn't in a country, there is not point continuing the address parsing:
            this.setCountry(null);
            Log.e(TAG, countryCodeResult.getError());
            invalidLocationToast.show();
            this.locationLayout.setVisibility(View.GONE);
            return;
        }

        // Set the city:
        final Result<String, String> cityResult = GeocodingUtil.getCity(location);
        isAddressValid = cityResult.isOk();
        if (cityResult.isOk())
            this.setCity(cityResult.getValue());
        else {
            this.setCity(null);
            Log.e(TAG, cityResult.getError());
        }

        // Set the address:
        final Result<String, String> addressResult = GeocodingUtil.getStreetAddress(location);
        isAddressValid &= addressResult.isOk();
        if (addressResult.isOk())
            this.setAddress(addressResult.getValue());
        else {
            this.setAddress(null);
            Log.e(TAG, addressResult.getError());
        }

        // if the location isn't valid show a toast message:
        if (!isAddressValid)
            invalidLocationToast.show();
    }

    /**
     * Sets the country in the location info layout according to a given country code.
     * @param countryCode The country code of the new country. If null or empty, the country code
     *                    picker, the country code edit text and the phone edit text will be hidden.
     *                    If it is a normal country code, the country will be displayed as usual.
     */
    private void setCountry(@Nullable String countryCode) {
        // If the address is not in a country:
        if (countryCode == null || countryCode.isEmpty()) {
            this.selectedCountry = null;
            Log.e(TAG, "Given address does not contain a country code");

            this.countryCodePicker.setVisibility(View.GONE);
            this.tilPhone.setVisibility(View.GONE);
            this.tilCountryCode.setVisibility(View.GONE);

            return;
        }

        Log.i(TAG, "Country code given: " + countryCode);

        // Am Israel Hai!
        if (countryCode.equals("PS"))
            countryCode = "IL";

        this.countryCodePicker.setVisibility(View.VISIBLE);
        this.tilPhone.setVisibility(View.VISIBLE);
        this.tilCountryCode.setVisibility(View.VISIBLE);

        this.countryCodePicker.setCountryForNameCode(countryCode);
        this.selectedCountry = this.countryCodePicker.getSelectedCountryName();
        this.etCountryCode.setText(this.countryCodePicker.getSelectedCountryCodeWithPlus());
    }

    /**
     * Sets the city in the location info layout according to a given city name.
     * @param city The name of the new city. If null or empty, the city edit text will not be shown.
     */
    private void setCity(@Nullable String city) {
        // if the city is null or empty:
        if (city == null || city.isEmpty()) {
            this.selectedCity = null;
            Log.e(TAG, "Given address does not contain a city");
            this.tilCity.setVisibility(View.GONE);
            return;
        }

        this.selectedCity = city;
        Log.i(TAG, "City given: " + city);

        // Show the city edit text and set the text to the new city:
        this.tilCity.setVisibility(View.VISIBLE);
        this.etCity.setText(city);
    }

    /**
     * Sets the address in the location info layout, according to the address string.
     * @param address The address string. Can be null.
     */
    private void setAddress(@Nullable String address) {
        // Check if the address is null or empty
        if (address == null || address.isEmpty()) {
            this.selectedAddress = null;
            Log.e(TAG, "Empty address given");
            this.tilAddress.setVisibility(View.GONE);
            return;
        }

        this.selectedAddress = address;
        Log.i(TAG, "Given address: " + address);

        // Set the additional address:
        this.tilAddress.setVisibility(View.VISIBLE);
        this.etAddress.setText(address);
    }

    private void hideLocationInfo() {
        // Hide the location info layout:
        this.locationLayout.setVisibility(View.GONE);

        // Show the progress bar:
        this.pbLocationLoader.setVisibility(View.VISIBLE);
    }

    private void initInputLayouts(View parent) {
        this.tilPhone = parent.findViewById(R.id.fragInput2TilPhoneNumber);
        this.tilCountryCode = parent.findViewById(R.id.fragInput2TilCountryCode);
        this.tilCity = parent.findViewById(R.id.fragInput2TilCity);
        this.tilAddress = parent.findViewById(R.id.fragInput2TilAddress);
    }

    private void initEditTexts(View parent) {
        this.etPhone = parent.findViewById(R.id.fragInput2EtPhoneNumber);
        this.etCountryCode = parent.findViewById(R.id.fragInput2EtCountryCode);
        this.etCity = parent.findViewById(R.id.fragInput2EtCity);
        this.etAddress = parent.findViewById(R.id.fragInput2EtAddress);
    }

    private void initCountryCodePicker(View parent) {
        this.countryCodePicker = parent.findViewById(R.id.fragInput2CountryCodePicker);

        // Bind the phone number edit text to the CCP:
        this.countryCodePicker.registerCarrierNumberEditText(this.etPhone);

        // Create a validator for the phone number:
        final Function<String, Result<Void, String>> validator = _input -> {
            if (this.countryCodePicker.isValidFullNumber())
                return Result.success(null);
            else if (Util.getTextFromEt(this.etPhone).isEmpty())
                return Result.failure(Constants.MANDATORY_INPUT_ERROR);
            else
                return Result.failure("Invalid phone number");
        };

        // Add the onValidityChanged listener for the CCP:
        this.countryCodePicker.setPhoneNumberValidityChangeListener(
                _b -> Util.validateAndSetError(this.tilPhone, this.etPhone, validator)
        );

        // Add textWatcher for the CCP:
        this.etPhone.addTextChangedListener(
                (ImprovedTextWatcher)
                        (_c, _i1, _i2, _i3) ->
                                Util.validateAndSetError(this.tilPhone, this.etPhone, validator)
        );

        // Change country code number:
        this.countryCodePicker.setOnCountryChangeListener(this::updateDisplayedCountryCode);
    }

    private void updateDisplayedCountryCode() {
        this.etCountryCode.setText(
                String.format(
                        Locale.getDefault(),
                        "+%s",
                        this.countryCodePicker.getSelectedCountryCode()
                )
        );
    }

    private boolean checkGooglePlayServices() {
        // Check if the service is available:
        final GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int result = googleApiAvailability.isGooglePlayServicesAvailable(requireContext());

        // Check if the result was a success:
        if (result == ConnectionResult.SUCCESS)
            return true;

        // Check if the user can resolve this error:
        else if (googleApiAvailability.isUserResolvableError(result)) {
            // Show the error dialog to the user:
            final Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201);
            if (dialog != null)
                dialog.show();
        }

        return false;
    }

    public boolean areInputsValid() {
        // Check the location:
        final boolean isLocationValid =  this.selectedCountry != null &&
                this.selectedCity != null &&
                this.selectedAddress != null;

        if (!isLocationValid) {
            Toast.makeText(requireContext(), "Please choose your address", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Return true if the phone is valid (error message is in the input field):
        return this.countryCodePicker.isValidFullNumber();
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
                this.countryCodePicker.getFullNumberWithPlus(),
                this.selectedCountry,
                this.selectedCity,
                this.selectedAddress
        );
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setOnMapClickListener(this);

        // Enable zoom controls:
        this.map.getUiSettings().setZoomControlsEnabled(true);

        // Load the last saved result if one exists:
        if (this.lastGeoResult != null)
            this.loadFromResult(this.lastGeoResult);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Update the marker on the map:
        this.setMarkerOnMap(latLng);

        // The user has picked a location, show the progress bar until the info is loaded:
        this.pbLocationLoader.setVisibility(View.VISIBLE);
        this.locationLayout.setVisibility(View.GONE);

        // Load the country, city and address from the location:
        this.loadInfoFromLocation(latLng.latitude, latLng.longitude);
    }

    private void loadInfoFromLocation(double latitude, double longitude) {
        // Create a handler that accepts the location returned from the thread and sets the
        // map to it:
        final Handler geoHandler = new Handler(
                Looper.getMainLooper(),
                this::handleGeocoderResult
        );

        // Create the thread and start it:
        final GeocodingThread geoThread = GeocodingThread.getReverseGeocoderThread(
                geoHandler,
                new com.google.maps.model.LatLng(latitude, longitude)
        );

        geoThread.start();

        // Hide the location details layout and show the progress bar:
        hideLocationInfo();
    }

    private void setMarkerOnMap(@NonNull LatLng latLng) {
        // Set marker:
        this.map.clear();
        this.map.addMarker(new MarkerOptions().position(latLng).title("Chosen Location"));
    }

    private void moveToLocation(@NonNull LatLng latLng) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
        this.map.animateCamera(cameraUpdate, ZOOM_DURATION, null);
    }

    private void moveToLocation(@NonNull LatLngBounds bounds) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        this.map.animateCamera(cameraUpdate, ZOOM_DURATION, null);
    }

    // Implement built-in life-cycle methods for the map:
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");

        if (this.isMapsApiAvailable)
            this.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // Check for API availability:
        this.isMapsApiAvailable = checkGooglePlayServices();
        if (this.isMapsApiAvailable)
            this.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");

        if (this.isMapsApiAvailable)
            this.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.isMapsApiAvailable)
            this.mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.isMapsApiAvailable)
            this.mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (this.isMapsApiAvailable)
            this.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (this.isMapsApiAvailable)
            this.mapView.onLowMemory();
    }
}
