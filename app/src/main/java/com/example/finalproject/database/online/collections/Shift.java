package com.example.finalproject.database.online.collections;

import java.util.Date;

public class Shift {
    // The ID of the shift:
    private String shiftId;

    // The ID of the user that works in the shift and the branch they are working at:
    private String uid, branchId;

    // The name of the company of the branch:
    private String companyName;

    // The role of the employee in the shift:
    private String role;

    // The date of the shift:
    private Date shiftDate;

    // The starting time and ending time of the shift (in minutes since midnight):
    private int startingTime, endingTime;

    // Empty constructor, per firestore's requirements
    public Shift() {}

    // Getters and setters:
    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(Date shiftDate) {
        this.shiftDate = shiftDate;
    }

    public int getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(int startingTime) {
        this.startingTime = startingTime;
    }

    public int getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(int endingTime) {
        this.endingTime = endingTime;
    }
}
