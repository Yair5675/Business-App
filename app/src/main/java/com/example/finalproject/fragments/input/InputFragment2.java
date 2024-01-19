package com.example.finalproject.fragments.input;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.database.entities.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class InputFragment2 extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    // The map that allows the user to choose a location:
    private GoogleMap map;

    // The view of that map:
    private MapView mapView;

    // The country that the user selected:
    private String selectedCountry;

    // The city that the user selected:
    private String selectedCity;

    // The address that the user selected:
    private String selectedAddress;

    /**
     * After validation occurred, the info given by the user needs to be given to the activity which
     * needs it. This class provides a convenient way for the info to pass anywhere.
     * Pay attention that although any class can READ the data that is saved, no class can create an
     * InputFragment2.PackagedInfo object, except the InputFragment2 class.
     */
    public static class PackagedInfo {
        public final String COUNTRY;
        public final String CITY;
        public final String ADDRESS;

        private PackagedInfo(String country, String city, String address) {
            COUNTRY = country;
            CITY = city;
            ADDRESS = address;
        }
    }

    public InputFragment2(String selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    public void loadInputsFromUser(User user) {
        // TODO: When switching to Firestore this function will have to be changed
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the second input fragment:
        final View parent = inflater.inflate(R.layout.fragment_input_2, container, false);

        // Initialize the map view:
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
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Update the marker on the map:
        this.setMarkerOnMap(latLng);

        // Load the country, city and address from the location:
        this.loadInfoFromLocation(latLng.latitude, latLng.longitude);
    }

    private void loadInfoFromLocation(double latitude, double longitude) {
        // TODO: Use reverse geocoding
    }

    private void setMarkerOnMap(@NonNull LatLng latLng) {
        this.map.clear();
        this.map.addMarker(new MarkerOptions().position(latLng).title("Chosen Location"));
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
