package com.example.finalproject.database.online.collections;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.functions.FirebaseFunctions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Branch implements Serializable {
    // The branch ID:
    private String branchId;

    // The company name:
    private String companyName;

    // The password for the branch:
    private String password;

    // The address, city and country of the branch:
    private String address, city, country;

    // The combined address (for debugging purposes):
    private String fullAddress;

    // The opening and closing time:
    private int openingTime, closingTime;

    // The number of shifts every day (starting at Sunday as the first index):
    private List<Integer> dailyShiftsNum;

    // Empty constructor. Requirement of firebase:
    public Branch() {

    }

    // Getters and setters:


    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public int getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(int openingTime) {
        this.openingTime = openingTime;
    }

    public int getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(int closingTime) {
        this.closingTime = closingTime;
    }

    public List<Integer> getDailyShiftsNum() {
        return this.dailyShiftsNum;
    }

    public void setDailyShiftsNum(List<Integer> dailyShiftsNum) {
        this.dailyShiftsNum = dailyShiftsNum;
    }

    public void fireUser(String uid, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Get an instance of firebase cloud functions (in middle east because the functions are
        // there):
        final String region = "me-west1";
        FirebaseFunctions functions = FirebaseFunctions.getInstance(region);

        // Call the fire_user_from_branch function with the user and branch IDs:
        final Map<String, String> data = new HashMap<>();
        data.put("uid", uid);
        data.put("branchId", this.branchId);
        functions
                .getHttpsCallable("fire_user_from_branch")
                .call(data)
                // The function shouldn't return anything if it succeeds:
                .addOnSuccessListener(_r -> onSuccessListener.onSuccess(null))
                .addOnFailureListener(onFailureListener);
    }
}
