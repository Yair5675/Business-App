package com.example.finalproject.database.online.collections;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Application {
    // A timestamp indicating when the application was submitted:
    @ServerTimestamp
    private Date submittedAt;

    // The ID and full name of the user applying to the branch:
    private String uid, userFullName;

    // The user's phone number:
    private String userPhoneNumber;

    // The path to the user's image in the storage:
    private String userImagePath;

    // Constants that show the attribute names in the database, instead of hardcoding them:
    public static final String UID = User.UID;
    public static final String USER_FULL_NAME = "userFullName";
    public static final String USER_IMAGE_PATH = "userImagePath";
    public static final String SUBMITTED_AT = "submittedAt";

    public Application() {}

    public Application(User user) {
        this.uid = user.getUid();
        this.userFullName = user.getFullName();
        this.userPhoneNumber = user.getPhoneNumber();
        this.userImagePath = user.getImagePath();
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserImagePath() {
        return userImagePath;
    }

    public void setUserImagePath(String userImagePath) {
        this.userImagePath = userImagePath;
    }
}
