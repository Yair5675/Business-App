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
import com.example.finalproject.thread.GeocodingThread;
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

    // The country that the user selected:
    private String selectedCountry;

    // The city that the user selected:
    private String selectedCity;

    // The address that the user selected:
    private String selectedAddress;

    // The layout containing all info about the selected location, will be shown only after
    // selection on the map:
    private LinearLayout locationLayout;

    // The country code picker that receives the user's phone number and country:
    private TextInputLayout tilPhone;
    private TextInputEditText etPhone;
    private CountryCodePicker countryCodePicker;

    // The edit code that displays the country code digits selected:
    private TextInputEditText etCountryCode;

    // The progress bar that will be shown while the reverse geocoding is happening:
    private ProgressBar pbLocationLoader;

    // The edit text holding the picked city:
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

        this.svMap.setOnSearchClickListener(_v -> {

            // Get the input from the user:
            CharSequence locationInput = this.svMap.getQuery();

            // Check if the location is empty:
            if (locationInput == null || locationInput.length() == 0) {
                Toast.makeText(
                        requireContext(),
                        "Please enter a location",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Create a handler that accepts the location returned from the thread and sets the map
            // to it:
            final Handler geoHandler = new Handler(Looper.getMainLooper(), this::handleGeocoderResult);

            // Create the thread and start it:
            final GeocodingThread geoThread = GeocodingThread.getGeocoderThread(
                    requireContext(),
                    geoHandler,
                    locationInput.toString()
            );

            geoThread.start();
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
            this.displayLocationInfo(address);
            this.setMarkerOnMap(new LatLng(address.getLatitude(), address.getLongitude()));

        } else {
            Log.e(TAG, result.getError().toString());
        }

        return false;
    }

    private void displayLocationInfo(Address address) {
        // TODO: Set country, city and address. Show the location info layout and hide the progress
        //  bar
    }

    private void initInputLayouts(View parent) {
        this.tilPhone = parent.findViewById(R.id.fragInput2TilPhoneNumber);
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
        // Update the marker on the map:
        this.setMarkerOnMap(latLng);

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

        // TODO: Separate the setMarkerOnMap and moveToLocation functions. Although it is convenient
        //  now, it is less flexible and goes against the separation of concerns principle
        // Move the camera to the marker:
        moveToLocation(latLng, Zoom.STREETS, 1000);
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
