package com.example.finalproject.database.online.collections;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

public class Branch implements Serializable {
    // The branch ID:
    private String branchId;

    // Whether the branch is active or not (if not, no new shifts/employees can be added to it):
    private boolean isActive;

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

    // The number of pending applications in the database:
    private int pendingApplications;

    // The number of shifts every day (starting at Sunday as the first index):
    private List<Integer> dailyShiftsNum;

    // Constants that show the attribute names in the database, instead of hardcoding them:
    public static final String BRANCH_ID = "branchId";
    public static final String PASSWORD = "password";
    public static final String COMPANY_NAME = "companyName";
    public static final String PENDING_APPLICATIONS = "pendingApplications";
    public static final String IS_ACTIVE = "active";
    public static final String ADDRESS = "address";
    public static final String CITY = "city";
    public static final String COUNTRY = "country";
    public static final String FULL_ADDRESS = "fullAddress";
    public static final String OPENING_TIME = "openingTime";
    public static final String CLOSING_TIME = "closingTime";
    public static final String DAILY_SHIFTS_NUM = "dailyShiftsNum";

    // Empty constructor. Requirement of firebase:
    public Branch() {}

    // Getters and setters:

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public int getPendingApplications() {
        return pendingApplications;
    }

    public void setPendingApplications(int pendingApplications) {
        this.pendingApplications = pendingApplications;
    }

    public List<Integer> getDailyShiftsNum() {
        return this.dailyShiftsNum;
    }

    public void setDailyShiftsNum(List<Integer> dailyShiftsNum) {
        this.dailyShiftsNum = dailyShiftsNum;
    }

    /**
     * Returns a reference in the database to the location of the current branch. Use this method
     * only if the branch ID was set.
     * @return A reference to the branch in the database.
     */
    public DocumentReference getReference() {
        return FirebaseFirestore.getInstance()
                .collection("branches")
                .document(this.branchId)
                ;
    }

    public String jsonifyBranch() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }
}
