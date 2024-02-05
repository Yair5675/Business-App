package com.example.finalproject.database.online.collections;

public class Employee {
    // The user's ID:
    private String uid;

    // Whether or not the user is a manager:
    private boolean isManager;

    // The name and surname of the employee:
    private String name, surname, fullName;

    // The path to the user's image in firebase storage:
    private String imagePath;

    // Empty constructor (firestore requirement):
    public Employee() {

    }

    // Getters and setters:

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        // Update the full name field:
        if (this.surname != null)
            this.setFullName(this.name + " " + this.surname);
        else
            this.setFullName(this.name);
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;

        // Update the full name field:
        if (this.name != null)
            this.setFullName(this.name + " " + this.surname);
        else
            this.setFullName(this.surname);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
