package com.example.finalproject.database.online.collections;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

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

    /**
     * A utility method that makes it easier to create an employee object from an existing user.
     * @param user A user whom the new employee object represent.
     * @param isManager Whether or not the user is a manager in the branch that owns the employee
     *                  object.
     * @return A new employee object consisting ot the given branch and the given isManager param.
     */
    public static Employee fromUser(User user, boolean isManager) {
        // Create the new employee object:
        final Employee employee = new Employee();
        employee.setUid(user.getUid());
        employee.setName(user.getName());
        employee.setSurname(user.getSurname());
        employee.setManager(isManager);
        employee.setImagePath(user.getImagePath());

        // Return it:
        return employee;
    }

    public String jsonifyEmployee() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        else if (this == obj)
            return true;
        else if (!(obj instanceof Employee))
            return false;
        else
            return this.uid.equals(((Employee) obj).uid);
    }
}
