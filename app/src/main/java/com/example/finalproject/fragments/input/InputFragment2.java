package com.example.finalproject.fragments.input;

import android.location.Address;
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
import com.example.finalproject.database.entities.User;
import com.example.finalproject.threads.GeocodingThread;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Util.Zoom;
import com.example.finalproject.util.ImprovedTextWatcher;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;

import java.util.Locale;
import java.util.function.Function;

public class InputFragment2 extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    // The map that allows the user to choose a location:
    private GoogleMap map;

    // The view of that map:
    private MapView mapView;

    // The search view allowing the user to search a location:
    private SearchView svMap;

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
    private TextInputEditText etAddress;

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

    public void loadInputsFromUser(User user) {
        // TODO: When switching to Firestore this function will have to be changed
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
        // Check if we have proper permissions and google services are available:
        if (checkMapPermission() && checkGooglePlayServices()) {
            // Use the onCreate life-cycle method on the map:
            this.mapView.onCreate(savedInstanceState);

            // Add the activity as a callback:
            this.mapView.getMapAsync(this);
        }

        return parent;
    }

    private void initSearchView(View parent) {
        this.svMap = parent.findViewById(R.id.fragInput2MapSearch);

        this.svMap.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Search button pressed");

                // Get the input from the user:
                CharSequence locationInput = svMap.getQuery();

                // Check if the location is empty:
                if (locationInput == null || locationInput.length() == 0) {
                    Toast.makeText(
                            requireContext(),
                            "Please enter a location",
                            Toast.LENGTH_SHORT
                    ).show();
                    return false;
                }
                Log.d(TAG, "Given query: " + locationInput);

                // Create a handler that accepts the location returned from the thread and sets the
                // map to it:
                final Handler geoHandler = new Handler(
                        Looper.getMainLooper(),
                        InputFragment2.this::handleGeocoderResult
                );

                // Create the thread and start it:
                final GeocodingThread geoThread = GeocodingThread.getGeocoderThread(
                        requireContext(),
                        geoHandler,
                        locationInput.toString()
                );

                geoThread.start();

                // Hide the location details layout and show the progress bar:
                hideLocationInfo();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // While the text is changing no geocoding operation is carried out
                return false;
            }
        });
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
            if (!(result.getValue() instanceof Address)) {
                Log.e(
                        TAG,
                        "Unexpected type for successful type returned from geocoding thread " +
                                "(expected Address): " + result.getValue().getClass().getName()
                );
                return false;
            }

            // Load the info from the address:
            final Address address = (Address) result.getValue();
            printAddress(address);
            this.displayLocationInfo(address);

            // Set the marker on the map and move to it:
            final LatLng coordinates = new LatLng(address.getLatitude(), address.getLongitude());
            this.setMarkerOnMap(coordinates);
            this.moveToLocation(coordinates, Zoom.STREETS, 1000);

        } else {
            Log.e(TAG, result.getError().toString());
        }

        return false;
    }

    private static void printAddress(Address address) {
        final int MAX_INDEX = address.getMaxAddressLineIndex();
        final StringBuilder builder = new StringBuilder();
        String line;
        for (int i = 0; i <= MAX_INDEX; i++) {
            if ((line = address.getAddressLine(i)) != null)
                builder.append(line);
        }

        Log.d(TAG, builder.toString());
    }

    private void displayLocationInfo(Address address) {
        // TODO: Set country, city and address. Show the location info layout and hide the progress
        //  bar
        // Show the location info layout:
        this.locationLayout.setVisibility(View.VISIBLE);

        // Hide the progress bar:
        this.pbLocationLoader.setVisibility(View.GONE);

        // Display the country while checking if the address is in a country:
        if (!this.setCountry(address.getCountryCode())) {
            // If the address isn't in a country, there is not point continuing the address parsing:
            Toast.makeText(requireContext(), "Invalid location", Toast.LENGTH_SHORT).show();
            this.locationLayout.setVisibility(View.GONE);
            return;
        }

        // Display the city while checking if the address is in a city:
        if (!this.setCity(address.getLocality())) {
            // Here we don't close the location info layout because it will still display the
            // country
            Toast.makeText(requireContext(), "Invalid location", Toast.LENGTH_SHORT).show();
        }

        // TODO: Display address next

    }

    /**
     * Sets the country in the location info layout according to a given country code.
     * @param countryCode The country code of the new country. If null or empty, the country code
     *                    picker, the country code edit text and the phone edit text will be hidden.
     *                    If it is a normal country code, the country will be displayed as usual.
     * @return True if the country code isn't null or empty, false otherwise.
     */
    private boolean setCountry(@Nullable String countryCode) {
        // If the address is not in a country:
        if (countryCode == null || countryCode.isEmpty()) {
            Log.e(TAG, "Given address does not contain a country code");

            this.countryCodePicker.setVisibility(View.GONE);
            this.tilPhone.setVisibility(View.GONE);
            this.tilCountryCode.setVisibility(View.GONE);

            return false;
        }

        Log.i(TAG, "Country code given: " + countryCode);

        // Am Israel Hai!
        if (countryCode.equals("PS"))
            countryCode = "IL";

        this.countryCodePicker.setVisibility(View.VISIBLE);
        this.tilPhone.setVisibility(View.VISIBLE);
        this.tilCountryCode.setVisibility(View.VISIBLE);

        this.countryCodePicker.setCountryForNameCode(countryCode);
        this.etCountryCode.setText(this.countryCodePicker.getSelectedCountryCodeWithPlus());

        return true;
    }

    /**
     * Sets the city in the location info layout according to a given city name.
     * @param city The name of the new city. If null or empty, the city edit text will not be shown.
     * @return True if the city name isn't null or empty, false otherwise.
     */
    private boolean setCity(@Nullable String city) {
        // if the city is null or empty:
        if (city == null || city.isEmpty()) {
            Log.e(TAG, "Given address does not contain a city");
            this.tilCity.setVisibility(View.GONE);
            return false;
        }

        Log.i(TAG, "City given: " + city);

        // Show the city edit text and set the text to the new city:
        this.tilCity.setVisibility(View.VISIBLE);
        this.etCity.setText(city);

        return true;
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
        // TODO: Complete the function and check for google play availability
        return true;
    }

    private boolean checkMapPermission() {
        // TODO: Complete the function and check for map and location permissions
        return true;
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
                this.countryCodePicker.getFullNumberWithPlus(),
                this.getCountry(),
                this.getCity(),
                this.getAddress()
        );
    }

    private String getCountry() {
        // TODO: Complete the function to return the country after the map view is finished
        return "";
    }

    private String getAddress() {
        // TODO: Complete the function to return the address after the map view is finished
        return "";
    }

    private String getCity() {
        // TODO: Complete the function to return the city after the map view is finished
        return "";
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setOnMapClickListener(this);

        // Enable zoom controls:
        this.map.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Update the marker on the map and move the map to it:
        this.setMarkerOnMap(latLng);
        this.moveToLocation(latLng, Zoom.STREETS, 1000);

        // The user has picked a location, show the progress bar until the info is loaded:
        this.pbLocationLoader.setVisibility(View.VISIBLE);
        this.locationLayout.setVisibility(View.GONE);

        // Load the country, city and address from the location:
        this.loadInfoFromLocation(latLng.latitude, latLng.longitude);
    }

    private void loadInfoFromLocation(double latitude, double longitude) {
        // TODO: Use reverse geocoding
    }

    private void setMarkerOnMap(@NonNull LatLng latLng) {
        // Set marker:
        this.map.clear();
        this.map.addMarker(new MarkerOptions().position(latLng).title("Chosen Location"));
    }

    private void moveToLocation(@NonNull LatLng latLng, Zoom zoomLevel, int duration) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel.level);
        this.map.animateCamera(cameraUpdate, duration, null);
    }

    // Implement built-in life-cycle methods for the map:
    @Override
    public void onStart() {
        super.onStart();

        this.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        this.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        this.mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        this.mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        this.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        this.mapView.onLowMemory();
    }
}
