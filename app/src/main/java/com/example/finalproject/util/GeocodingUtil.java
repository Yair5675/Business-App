package com.example.finalproject.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;

public class GeocodingUtil {
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
}
