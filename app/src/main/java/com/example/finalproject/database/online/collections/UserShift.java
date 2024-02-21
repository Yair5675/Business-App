package com.example.finalproject.database.online.collections;

import java.util.Date;

public class UserShift {
    // The user's ID:
    private String uid;

    // The branch's ID:
    private String branchId;

    // The role of the user:
    private String roleName;

    // The name of the company of the shift:
    private String companyName;

    // The date of the shift:
    private Date date;

    // The starting and ending time of the shift:
    private int startingTime, endingTime;

    public UserShift() {
        // Public empty constructor for firestore
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }
}
