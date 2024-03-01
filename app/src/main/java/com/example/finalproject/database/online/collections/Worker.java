package com.example.finalproject.database.online.collections;

public class Worker {
    // TODO: Delete the class, per the new database design

    // The uid of the worker:
    private String uid;

    // The name and surname of the user:
    private String name, surname;

    // Whether the worker is a manager or not:
    private boolean isManager;

    // The path to the user's image in the storage:
    private String imagePath;

    // The name of the role of the worker:
    private String roleName;

    public Worker() {
        // Public empty constructor (firestore requirements)
    }

    public Worker(Employee employee, String roleName) {
        this.uid = employee.getUid();
        this.name = employee.getName();
        this.surname = employee.getSurname();
        this.isManager = employee.isManager();
        this.imagePath = employee.getImagePath();
        this.roleName = roleName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
