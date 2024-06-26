package com.example.finalproject.fragments;

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
import com.example.finalproject.threads.GeocodingThread;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.GeocodingUtil;
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

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    // The map that allows the user to choose a location:
    private GoogleMap map;

    // The view of that map:
    private MapView mapView;

    // The layout containing all info about the selected location, will be shown only after
    // selection on the map:
    private LinearLayout locationLayout;

    // The country code picker that receives the user's phone number and country:
    private @Nullable TextInputLayout tilPhone;
    private @Nullable TextInputEditText etPhone;
    private CountryCodePicker countryCodePicker;

    // The edit text that displays the country code digits selected:
    private @Nullable TextInputLayout tilCountryCode;
    private @Nullable TextInputEditText etCountryCode;

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

    // The last result from the geocoding API. Is used to load a previously saved location
    private GeocodingResult lastGeoResult;

    // A variable to handle if the google API is available:
    private boolean isMapsApiAvailable;

    // The country that the map must return:
    private String restrictedCountry;

    // The duration of the zoom animation in milliseconds:
    private static final int ZOOM_DURATION = 1000;

    // The zoom level used if no bounds are returned in the response:
    public static final int DEFAULT_ZOOM_LEVEL = Util.Zoom.STREETS.level;

    // A tag for logging purposes:
    private static final String TAG = "MapFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the map fragment XML:
        final View parent = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize the edit texts and input layouts for them:
        this.initEditTexts(parent);
        this.initTextLayouts(parent);

        // Re-establish the CCP:
        this.countryCodePicker = parent.findViewById(R.id.fragMapCountryCodePicker);

        // Initialize the search view and load the onSearchListener:
        this.initSearchView(parent);

        // Load the progress bar and set it to gone:
        this.pbLocationLoader = parent.findViewById(R.id.fragMapPbLocationLoader);
        this.pbLocationLoader.setVisibility(View.GONE);

        // Load the location layout:
        this.locationLayout = parent.findViewById(R.id.fragMapLayoutLocationDetails);

        // Hide all the location details:
        this.locationLayout.setVisibility(View.GONE);

        // Initialize the map:
        this.mapView = parent.findViewById(R.id.fragMapMapView);
        // Check that the service is available:
        this.isMapsApiAvailable = checkGooglePlayServices();
        if (this.isMapsApiAvailable) {
            // Use the onCreate life-cycle method on the map:
            this.mapView.onCreate(savedInstanceState);

            // Add the activity as a callback:
            this.mapView.getMapAsync(this);
        }

        return parent;
    }

    private void initEditTexts(View parent) {
        this.etCity = parent.findViewById(R.id.fragMapEtCity);
        this.etAddress = parent.findViewById(R.id.fragMapEtAddress);
    }

    private void initTextLayouts(View parent) {
        this.tilCity = parent.findViewById(R.id.fragMapTilCity);
        this.tilAddress = parent.findViewById(R.id.fragMapTilAddress);
    }

    public void connectPhoneInput (
            @NonNull TextInputLayout tilCountryCode, @NonNull TextInputEditText etCountryCode,
            @NonNull TextInputLayout tilPhone,  @NonNull TextInputEditText etPhone
    ) {
        // Load the pointers:
        this.tilCountryCode = tilCountryCode;
        this.tilPhone = tilPhone;
        this.etCountryCode = etCountryCode;
        this.etPhone = etPhone;

        // Load the validation in the ccp:
        if (this.countryCodePicker != null) {
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

            // Apply the validator:
            this.loadPhoneValidator(validator);
        }
    }

    private void loadPhoneValidator(Function<String, Result<Void, String>> validator) {
        if (this.tilPhone != null && this.etPhone != null) {
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
    }

    private void updateDisplayedCountryCode() {
        if (this.etCountryCode != null)
            this.etCountryCode.setText(
                    String.format(
                            Locale.getDefault(),
                            "+%s",
                            this.countryCodePicker.getSelectedCountryCode()
                    )
            );
    }

    private void initSearchView(View parent) {
        // The search view allowing the user to search a location:
        final SearchView svMap = parent.findViewById(R.id.fragMapMapSearch);

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

    public void loadFromLocation(String location) {
        if (!isMapsApiAvailable) {
            Toast.makeText(requireContext(), "Google Maps is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the location is empty:
        if (location == null || location.isEmpty()) {
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
                this::handleGeocoderResult
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

            // Hide the views:
            this.countryCodePicker.setVisibility(View.GONE);
            if (this.tilPhone != null && this.tilCountryCode != null) {
                this.tilPhone.setVisibility(View.GONE);
                this.tilCountryCode.setVisibility(View.GONE);
            }

            return;
        }

        Log.i(TAG, "Country code given: " + countryCode);

        // Am Israel Hai!
        if (countryCode.equals("PS"))
            countryCode = "IL";

        final String prevCountryCode = this.countryCodePicker.getSelectedCountryNameCode();
        this.countryCodePicker.setCountryForNameCode(countryCode);
        final String countryName = this.countryCodePicker.getSelectedCountryName();
        if (this.restrictedCountry != null && !countryName.equals(this.restrictedCountry)) {
            Toast.makeText(requireContext(), "Country must be " + this.restrictedCountry, Toast.LENGTH_SHORT).show();
            this.countryCodePicker.setCountryForNameCode(prevCountryCode);
            return;
        }

        // Show the views and set the appropriate texts:
        this.countryCodePicker.setVisibility(View.VISIBLE);

        if (this.tilPhone != null && this.tilCountryCode != null && this.etCountryCode != null) {
            this.tilPhone.setVisibility(View.VISIBLE);
            this.tilCountryCode.setVisibility(View.VISIBLE);
            this.etCountryCode.setText(this.countryCodePicker.getSelectedCountryCodeWithPlus());
        }

        this.selectedCountry = this.countryCodePicker.getSelectedCountryName();
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

        // Correct google's uncertainty about Tel Aviv:
        if (this.selectedCity.equals("Tel Aviv-Jaffa")) {
            Log.i(TAG, "Had to correct google's stupid mistake about Tel aviv");
            this.selectedCity = "Tel Aviv-Yafo";
        }

        // Show the city edit text and set the text to the new city:
        this.tilCity.setVisibility(View.VISIBLE);
        this.etCity.setText(this.selectedCity);
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

    private void hideLocationInfo() {
        // Hide the location info layout:
        this.locationLayout.setVisibility(View.GONE);

        // Hide all phone components if they exist:
        if (this.tilPhone != null && this.tilCountryCode != null) {
            this.tilPhone.setVisibility(View.GONE);
            this.tilCountryCode.setVisibility(View.GONE);
        }

        // Show the progress bar:
        this.pbLocationLoader.setVisibility(View.VISIBLE);
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

    /** @noinspection SameReturnValue*/
    private boolean handleBoundsResult(@NonNull Message message) {
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

            // Load the bounds:
            final Result<LatLngBounds, String> boundsResult = GeocodingUtil.getBounds((GeocodingResult) result.getValue());
            if (boundsResult.isOk()) {
                // Set the bounds:
                this.map.setLatLngBoundsForCameraTarget(boundsResult.getValue());
                // Zoom in to the bounds:
                this.moveToLocation(boundsResult.getValue());
                // Set the country:
                final Result<String, String> countryCodeResult = GeocodingUtil.getCountryCode((GeocodingResult) result.getValue());
                this.setCountry(countryCodeResult.isOk() ? countryCodeResult.getValue() : null);
            }
            else
                Log.e(TAG, "Couldn't get bounds from geocoding result: " + boundsResult.getError());

            // Remove the progress bar:
            this.pbLocationLoader.setVisibility(View.GONE);

        } else {
            // Remove the progress bar and alert the user:
            Log.e(TAG, result.getError().toString());
            this.pbLocationLoader.setVisibility(View.GONE);

            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        }

        return false;
    }
    public void restrictToCountry(String country) {
        if (isMapsApiAvailable && country != null && !country.isEmpty()) {
            // Hide the location details layout and show the progress bar:
            hideLocationInfo();

            // Create a handler:
            final Handler geoHandler = new Handler(
                    Looper.getMainLooper(),
                    this::handleBoundsResult
            );

            // Create the thread and start it:
            final GeocodingThread geoThread = GeocodingThread.getGeocoderThread(
                    geoHandler,
                    country
            );

            geoThread.start();

            // Save the restricted country:
            this.restrictedCountry = country;
        }
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

    // Implement built-in life-cycle methods for the map:
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");

        if (this.isMapsApiAvailable) {
            this.mapView.onStart();

            // Load the last saved result if one exists:
            if (this.map != null && this.lastGeoResult != null)
                this.loadFromResult(this.lastGeoResult);
        }
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

    // Getters:

    public String getSelectedCountry() {
        return selectedCountry;
    }

    public String getSelectedCity() {
        return selectedCity;
    }

    public String getSelectedAddress() {
        return selectedAddress;
    }

    public String getFullNumberWithPlus() {
        return this.countryCodePicker.getFullNumberWithPlus();
    }

    // Setter for the phone number:
    public void setPhoneNumber(String phoneNumber) {
        this.countryCodePicker.setFullNumber(phoneNumber);
    }

    public boolean isPhoneValid() {
        return this.countryCodePicker.isValidFullNumber();
    }
}
