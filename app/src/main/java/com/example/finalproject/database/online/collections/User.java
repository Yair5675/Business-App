package com.example.finalproject.database.online.collections;

import com.example.finalproject.BuildConfig;
import com.google.firebase.Timestamp;

public class User {
    // The user ID (generated from authentication):
    private String uid;

    // The name and surname of the user:
    private String name, surname;

    // The email, password and phone number of the user:
    private String email, password, phoneNumber;

    // The birthdate of the user:
    private Timestamp birthdate;

    // The city, country and address of the user:
    private String city, country, address;

    // The path to the user's image in firebase storage:
    private String imagePath;

    // An empty constructor (requirement for firestore):
    public User() {}

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
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public User setSurname(String surname) {
        this.surname = surname;
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

    public Timestamp getBirthdate() {
        return birthdate;
    }

    public User setBirthdate(Timestamp birthdate) {
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
}
