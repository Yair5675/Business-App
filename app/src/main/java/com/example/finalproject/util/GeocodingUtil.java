package com.example.finalproject.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

public class GeocodingUtil {
    public static final String NO_RESULTS_ERROR = "No location was found";

    public static void logResult(GeocodingResult result, String tag) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Log.i(tag, gson.toJson(result));
    }

    public static Result<String, String> getCountryCode(GeocodingResult geocodingResult) {
        for (AddressComponent component : geocodingResult.addressComponents) {
            for (AddressComponentType type : component.types) {
                if (type == AddressComponentType.COUNTRY)
                    return Result.success(component.shortName);
            }
        }

        return Result.failure("No country found in geocoding result");
    }

    public static Result<String, String> getCity(GeocodingResult geocodingResult) {
        for (AddressComponent component : geocodingResult.addressComponents) {
            for (AddressComponentType type : component.types) {
                if (type == AddressComponentType.LOCALITY)
                    return Result.success(component.longName);
            }
        }

        return Result.failure("No city found in geocoding result");
    }

    public static Result<String, String> getStreetAddress(GeocodingResult geocodingResult) {
        String route = null, streetNumber = null;
        for (AddressComponent component : geocodingResult.addressComponents) {
            for (AddressComponentType type : component.types) {
                if (type == AddressComponentType.ROUTE)
                    route = component.longName;
                else if (type == AddressComponentType.STREET_NUMBER)
                    streetNumber = component.longName;
            }
        }
        if (route == null)
            return Result.failure("Address was not found in geocoding result");
        else if (streetNumber == null)
            return Result.failure("Street number was not found in geocoding result");
        else
            return Result.success(route + " " + streetNumber);
    }

    public static Result<LatLngBounds, String> getBounds(GeocodingResult geocodingResult) {
        // Get north-east and south-west borders:
        if (geocodingResult.geometry.bounds == null)
            return Result.failure("Missing bounds for result");

        final LatLng NE = geocodingResult.geometry.bounds.northeast;
        final LatLng SW = geocodingResult.geometry.bounds.southwest;

        // Make sure they're not null and then convert them to other google latitude-longitude
        // objects:
        if (NE == null || SW == null)
            return Result.failure("Missing bounds for result");

        final com.google.android.gms.maps.model.LatLng northeast =
                new com.google.android.gms.maps.model.LatLng(NE.lat, NE.lng);
        final com.google.android.gms.maps.model.LatLng southwest =
                new com.google.android.gms.maps.model.LatLng(SW.lat, SW.lng);

        return Result.success(new LatLngBounds(southwest, northeast));
    }
}
