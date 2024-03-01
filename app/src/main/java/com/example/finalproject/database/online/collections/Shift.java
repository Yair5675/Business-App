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
    private String roleName;

    // The date of the shift:
    private Date shiftDate;

    // The starting time and ending time of the shift (in minutes since midnight):
    private int startingTime, endingTime;

    // Constants that show the attribute names in the database, instead of hardcoding them:
    public static final String SHIFT_ID = "shiftId";
    public static final String UID = User.UID;
    public static final String BRANCH_ID = Branch.BRANCH_ID;
    public static final String COMPANY_NAME = Branch.COMPANY_NAME;
    public static final String ROLE_NAME = "roleName";
    public static final String SHIFT_DATE = "shiftDate";
    public static final String STARTING_TIME = "startingTime";
    public static final String ENDING_TIME = "endingTime";

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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
