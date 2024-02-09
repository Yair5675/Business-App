package com.example.finalproject.database.online.collections;

import androidx.annotation.NonNull;

import com.example.finalproject.BuildConfig;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    // The user ID (generated from authentication):
    private String uid;

    // The name and surname of the user:
    private String name, surname, fullName;

    // The email, password and phone number of the user:
    private String email, password, phoneNumber;

    // The birthdate of the user:
    private Date birthdate;

    // The city, country and address of the user:
    private String city, country, address;

    // The path to the user's image in firebase storage:
    private String imagePath;

    // The minimum and maximum age of the user:
    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 80;

    // An empty constructor (requirement for firestore):
    public User() {}

    // A copy constructor:
    public User(@NonNull User other) {
        this.uid = other.uid;
        this.name = other.name;
        this.surname = other.surname;
        this.fullName = other.fullName;
        this.email = other.email;
        this.password = other.password;
        this.phoneNumber = other.phoneNumber;
        this.birthdate = (Date) other.birthdate.clone();
        this.city = other.city;
        this.country = other.country;
        this.address = other.address;
        this.imagePath = other.imagePath;
    }

    public boolean isAdmin() {
        return this.phoneNumber != null && this.phoneNumber.equals(BuildConfig.ADMIN_PHONE_NUMBER);
    }

    // Getters (a requirement) and setters supporting the builder pattern:
    public String getUid() {
        return uid;
    }

    public User setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;

        // Update the full name field:
        if (this.surname != null)
            this.setFullName(this.name + " " + this.surname);
        else
            this.setFullName(this.name);
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    private void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSurname() {
        return surname;
    }

    public User setSurname(String surname) {
        this.surname = surname;

        // Update the full name field:
        if (this.name != null)
            this.setFullName(this.name + " " + this.surname);
        else
            this.setFullName(this.surname);
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public User setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public Date getBirthdate() {
        return this.birthdate;
    }

    public User setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
        return this;
    }

    public String getCity() {
        return city;
    }

    public User setCity(String city) {
        this.city = city;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public User setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public User setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getImagePath() {
        return imagePath;
    }

    public User setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    public static int getMinAge() {
        return MIN_AGE;
    }

    public static int getMaxAge() {
        return MAX_AGE;
    }
}
