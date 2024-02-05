package com.example.finalproject.database.online.collections;

public class Workplace {
    // The branch's ID:
    private String branchId;

    // Whether or not the user is a manager at the branch:
    private boolean isManager;

    // The company name:
    private String companyName;

    // The country, city and address of the branch:
    private String country, city, address;

    // Empty constructor, firestore requirement:
    public Workplace() {

    }

    // Getters and setters:

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
