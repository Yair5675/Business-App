package com.example.finalproject.threads;

import android.os.Handler;
import android.os.Message;

import com.example.finalproject.BuildConfig;
import com.example.finalproject.util.GeocodingUtil;
import com.example.finalproject.util.Result;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.io.IOException;

public class GeocodingThread extends Thread {
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

    // The geo api context, will be used in all API requests:
    private static GeoApiContext context;

    private GeocodingThread(Handler handler, GeoService service) {
        this.handler = handler;
        this.service = service;

        // Set the new context only if it hadn't been set already:
        if (GeocodingThread.context == null)
            GeocodingThread.context = new GeoApiContext.Builder()
                    .apiKey(BuildConfig.MAPS_API_KEY)
                    .maxRetries(2)
                    .build();
    }

    public static GeocodingThread getGeocoderThread(Handler handler, String locationName) {
        // Set the service to be normal geocoding and create the thread:
        GeoService service = new GeoService(locationName, null);

        return new GeocodingThread(handler, service);
    }

    public static GeocodingThread getReverseGeocoderThread(Handler handler, LatLng coordinates) {
        // Set the service to be reverse geocoding and create the thread:
        GeoService service = new GeoService(null, coordinates);

        return new GeocodingThread(handler, service);
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
            final GeocodingResult[] results = GeocodingApi.reverseGeocode(context, this.service.coordinates).await();


            // Return the first result if one exists:
            Result<GeocodingResult, String> result;

            if (results.length == 0)
                result = Result.failure(GeocodingUtil.NO_RESULTS_ERROR);
            else
                result = Result.success(results[0]);

            // Send the result back to the creator of the thread:
            this.sendResultBack(result);

        } catch (IOException | ApiException | InterruptedException e) {
            // Send the error message back:
            this.sendResultBack(Result.failure(e.toString()));
        }
    }

    private void runNormalGeocoding() {
        try {
            // Get the results from the google maps API:
            final GeocodingResult[] results = GeocodingApi.geocode(context, this.service.locationName).await();

            // Return the first result if one exists:
            Result<GeocodingResult, String> result;

            if (results.length == 0)
                result = Result.failure(GeocodingUtil.NO_RESULTS_ERROR);
            else
                result = Result.success(results[0]);

            // Send the result back to the creator of the thread:
            this.sendResultBack(result);

        } catch (IOException | ApiException | InterruptedException e) {
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
