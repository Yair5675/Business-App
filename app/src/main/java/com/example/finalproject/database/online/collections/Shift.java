package com.example.finalproject.database.online.collections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;
import java.util.Date;

public class Shift implements Externalizable {
    // The ID of the shift:
    private String shiftId;

    // The ID of the user that works in the shift and the branch they are working at:
    private String uid, branchId;

    // The full name of the employee that works the shift:
    private String userFullName;

    // The name of the company of the branch:
    private String companyName;

    // The role of the employee in the shift:
    private String roleName;

    // The starting time and ending time of the shift:
    private Date startingTime, endingTime;

    // Constants that show the attribute names in the database, instead of hardcoding them:
    public static final String SHIFT_ID = "shiftId";
    public static final String UID = User.UID;
    public static final String BRANCH_ID = Branch.BRANCH_ID;
    public static final String USER_FULL_NAME = "userFullName";
    public static final String COMPANY_NAME = Branch.COMPANY_NAME;
    public static final String ROLE_NAME = "roleName";
    public static final String STARTING_TIME = "startingTime";
    public static final String ENDING_TIME = "endingTime";

    // Empty constructor, per firestore's requirements
    public Shift() {}

    // Additional constructor to make my life easier
    public Shift(
            String uid, String branchId, String userFullName, String companyName, String roleName,
            Date startingTime, Date endingTime
    ) {
        this.uid = uid;
        this.branchId = branchId;
        this.userFullName = userFullName;
        this.companyName = companyName;
        this.roleName = roleName;
        this.startingTime = startingTime;
        this.endingTime = endingTime;
    }

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

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
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

    /**
     * Returns the hour at which the shift starts.
     * @return The hour when the shift starts.
     */
    public int startHour() {
        return getDateField(this.startingTime, Calendar.HOUR_OF_DAY);
    }

    /**
     * Returns the minutes at which the shift starts (from 0 to 59).
     * @return The minutes at which the shift starts.
     */
    public int startMinutes() {
        return getDateField(this.startingTime, Calendar.MINUTE);
    }

    /**
     * Returns the hour at which the shift ends.
     * @return The hour when the shift ends.
     */
    public int endHour() {
        return getDateField(this.endingTime, Calendar.HOUR_OF_DAY);
    }

    /**
     * Returns the minutes at which the shift ends (from 0 to 59).
     * @return The minutes at which the shift ends.
     */
    public int endMinute() {
        return getDateField(this.endingTime, Calendar.MINUTE);
    }

    private static int getDateField(Date date, int field) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(field);
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }

    public Date getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(Date endingTime) {
        this.endingTime = endingTime;
    }

    @Override
    public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
        // Read the attributes according to the order they were saved:
        this.shiftId = (String) in.readObject();
        this.uid = (String) in.readObject();
        this.branchId = (String) in.readObject();
        this.companyName = (String) in.readObject();
        this.roleName = (String) in.readObject();
        this.startingTime = new Date(in.readLong());
        this.endingTime = new Date(in.readLong());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // Save the attributes (in a specific order):
        out.writeChars(this.shiftId);
        out.writeChars(this.uid);
        out.writeChars(this.branchId);
        out.writeChars(this.companyName);
        out.writeChars(this.roleName);
        out.writeLong(this.startingTime.getTime());
        out.writeLong(this.endingTime.getTime());
    }
}
