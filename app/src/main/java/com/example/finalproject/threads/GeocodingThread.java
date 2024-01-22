package com.example.finalproject.threads;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Message;

import com.example.finalproject.util.Result;
import com.google.type.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodingThread extends Thread {
    // The geocoder itself:
    private final Geocoder geocoder;

    // The service that this thread will provide (either geocoding or reverse-geocoding):
    private final GeoService service;

    private static class GeoService {
        // The location name (in case the service is normal geocoding):
        public final String locationName;

        // The coordinates of the location (in case the service is reverse-geocoding):
        public final LatLng coordinates;

        private GeoService(String locationName, LatLng coordinates) {
            this.locationName = locationName;
            this.coordinates = coordinates;
        }

        public boolean isReverseGeocoder() {
            return this.coordinates != null;
        }
    }

    // The handler of the thread, enables the thread to return its results to the calling thread:
    private final Handler handler;

    private GeocodingThread(Context context, Handler handler, GeoService service) {
        this.geocoder = new Geocoder(context, Locale.US);
        this.handler = handler;
        this.service = service;
    }

    public static GeocodingThread getGeocoderThread(Context context, Handler handler, String locationName) {
        // Set the service to be normal geocoding and create the thread:
        GeoService service = new GeoService(locationName, null);

        return new GeocodingThread(context, handler, service);
    }

    public static GeocodingThread getReverseGeocoderThread(Context context, Handler handler, LatLng coordinates) {
        // Set the service to be reverse geocoding and create the thread:
        GeoService service = new GeoService(null, coordinates);

        return new GeocodingThread(context, handler, service);
    }

    @Override
    public void run() {
        super.run();

        // Activate either normal geocoding or reverse geocoding:
        if (this.service.isReverseGeocoder())
            this.runReverseGeocoding();
        else
            this.runNormalGeocoding();
    }

    private void runReverseGeocoding() {
        try  {
            // Get the address from the coordinates:
            final double latitude = this.service.coordinates.getLatitude(),
                         longitude = this.service.coordinates.getLongitude();
            final List<Address> addresses = this.geocoder.getFromLocation(latitude, longitude, 1);

            // The information will be sent back as a Result object:
            Result<Address, String> result;

            if (addresses == null || addresses.isEmpty())
                result = Result.failure("Reverse geocoding failed: No results");
            else
                result = Result.success(addresses.get(0));

            // Send the result back to the creator of the thread:
            this.sendResultBack(result);
        } catch (IOException e) {
            // Send the error message back:
            this.sendResultBack(Result.failure(e.toString()));
        }
    }

    private void runNormalGeocoding() {
        try {
            // Get the address matching the name:
            final List<Address> addresses = this.geocoder.getFromLocationName(this.service.locationName, 1);

            // The information will be sent back as a Result object:
            Result<Address, String> result;

            if (addresses == null || addresses.isEmpty())
                result = Result.failure("Geocoding failed: No results");
            else
                result = Result.success(addresses.get(0));

            // Send the result back to the creator of the thread:
            this.sendResultBack(result);

        } catch (IOException e) {
            // Send the error message back:
            this.sendResultBack(Result.failure(e.toString()));
        }
    }

    private void sendResultBack(Result<?, ?> result) {
        final Message message = new Message();
        message.obj = result;
        this.handler.sendMessage(message);
    }
}
