package com.example.finalproject.database.online.collections.notifications;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public abstract class Notification {
    // The ID of the notification:
    private String notificationId;

    // Description for the notification:
    private String description;

    // The type of the notification, necessary because firestore doesn't support inheritance:
    private NotificationType type;

    // A timestamp indicating when the notification was saved in the database:
    @ServerTimestamp
    private Date timestamp;

    /**
     * An enum for all possible types of the notifications.
     */
    public enum NotificationType {
        EMPLOYEE_ACTION
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

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
}
