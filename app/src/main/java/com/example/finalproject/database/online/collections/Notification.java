package com.example.finalproject.database.online.collections;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Notification {
    // Description for the notification:
    private String description;

    // The type of the notification:
    private NotificationType type;

    // A timestamp indicating when the notification was saved in the database:
    @ServerTimestamp
    private Date timestamp;

    // TODO: Convert into an abstract class with inheritors that hold more info
    /**
     * An enum for all possible types of the notifications.
     */
    public enum NotificationType {
        PROMOTED,
        DEMOTED,
        FIRED,
        APPLIED,
        HIRED
    }

    public Notification() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
