package com.example.finalproject.database.online.collections.notifications;

import java.util.Locale;

public class EmployeeActionNotification extends Notification {
    // The ID and name of the user that the action applies to:
    private String uid, userName;

    // The ID and name of the branch that the action occurred in:
    private String branchId, branchName;

    // The employee action type:
    private EmployeeActionType actionType;

    public EmployeeActionNotification() {}

    private EmployeeActionNotification(
            String uid,
            String userName,
            String branchId,
            String branchName,
            EmployeeActionType actionType
    ) {
        // Set type for super-class:
        this.setType(NotificationType.EMPLOYEE_ACTION);

        // Set other details:
        this.uid = uid;
        this.userName = userName;
        this.branchId = branchId;
        this.branchName = branchName;
        this.actionType = actionType;

        // Set description from action type:
        this.setDescription(this.actionType.getDescription(userName, branchName));
    }

    // An enum containing all possible employee action types:
    public enum EmployeeActionType {
        PROMOTED {
            @Override
            public String getDescription(String userName, String branchName) {
                return String.format(Locale.getDefault(), "%s was promoted at %s", userName, branchName);
            }
        },
        DEMOTED {
            @Override
            public String getDescription(String userName, String branchName) {
                return String.format(Locale.getDefault(), "%s was demoted at %s", userName, branchName);
            }
        },
        FIRED {
            @Override
            public String getDescription(String userName, String branchName) {
                return String.format(Locale.getDefault(), "%s was fired from %s", userName, branchName);
            }
        },
        APPLIED {
            @Override
            public String getDescription(String userName, String branchName) {
                return String.format(Locale.getDefault(), "%s applied to %s", userName, branchName);
            }
        },
        REJECTED {
            @Override
            public String getDescription(String userName, String branchName) {
                return String.format(Locale.getDefault(), "%s was rejected by %s", userName, branchName);
            }
        },
        ACCEPTED {
            @Override
            public String getDescription(String userName, String branchName) {
                return String.format(Locale.getDefault(), "%s was accepted by %s", userName, branchName);
            }
        }
        ;
        public abstract String getDescription(String userName, String branchName);
    }

    // Factory pattern for easy creation:
    public static EmployeeActionNotification promotedNotification(
            String uid, String userName, String branchId, String branchName
    ) {
        return new EmployeeActionNotification(uid, userName, branchId, branchName, EmployeeActionType.PROMOTED);
    }

    public static EmployeeActionNotification demotedNotification(
            String uid, String userName, String branchId, String branchName
    ) {
        return new EmployeeActionNotification(uid, userName, branchId, branchName, EmployeeActionType.DEMOTED);
    }

    public static EmployeeActionNotification firedNotification(
            String uid, String userName, String branchId, String branchName
    ) {
        return new EmployeeActionNotification(uid, userName, branchId, branchName, EmployeeActionType.FIRED);
    }

    public static EmployeeActionNotification appliedNotification(
            String uid, String userName, String branchId, String branchName
    ) {
        return new EmployeeActionNotification(uid, userName, branchId, branchName, EmployeeActionType.APPLIED);
    }

    public static EmployeeActionNotification rejectedNotification(
            String uid, String userName, String branchId, String branchName
    ) {
        return new EmployeeActionNotification(uid, userName, branchId, branchName, EmployeeActionType.REJECTED);
    }

    public static EmployeeActionNotification acceptedNotification(
            String uid, String userName, String branchId, String branchName
    ) {
        return new EmployeeActionNotification(uid, userName, branchId, branchName, EmployeeActionType.ACCEPTED);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public EmployeeActionType getActionType() {
        return actionType;
    }

    public void setActionType(EmployeeActionType actionType) {
        this.actionType = actionType;
    }
}
