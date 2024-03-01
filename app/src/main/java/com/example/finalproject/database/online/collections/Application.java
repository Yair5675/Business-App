package com.example.finalproject.database.online.collections;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Application {
    // A timestamp indicating when the application was submitted:
    @ServerTimestamp
    private Date submittedAt;

    // The ID and full name of the user applying to the branch:
    private String uid, userFullName;

    // The path to the user's image in the storage:
    private String userImagePath;

    public Application() {}

    public Application(String uid, String userFullName, String userImagePath) {
        this.uid = uid;
        this.userFullName = userFullName;
        this.userImagePath = userImagePath;
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
