package com.example.finalproject.database.online.collections;

public class Branch {
    // The branch ID:
    private String branchId;

    // The company name:
    private String companyName;

    // The password for the branch:
    private String password;

    // The address, city and country of the branch:
    private String address, city, country;

    // The opening and closing time:
    private int openingTime, closingTime;

    // The number of shifts every day (starting at Sunday as the first index):
    private int[] dailyShiftsNum;

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

    public int[] getDailyShiftsNum() {
        return dailyShiftsNum;
    }

    public void setDailyShiftsNum(int[] dailyShiftsNum) {
        this.dailyShiftsNum = dailyShiftsNum;
    }
}
