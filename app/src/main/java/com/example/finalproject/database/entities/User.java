package com.example.finalproject.database.entities;

import android.content.Context;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.database.Converters;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A class that holds information about the user currently connected to the app. This class is a
 * singleton because only one user can be connected. The class is necessary because various
 * activities may need access to the user's details.
 */
@Entity(
        tableName = "TblUsers",
        foreignKeys =
        @ForeignKey
                (
                    entity = City.class, parentColumns = "cityId", childColumns = "cityId",
                    onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE
                )
        )
public class User {
    // The user id:
    @ColumnInfo(name = "userId")
    @PrimaryKey(autoGenerate = true)
    private long userId;

    // The various attributes of the user:
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "surname")
    private String surname;
    @ColumnInfo(name = "gender")
    private String gender;
    @ColumnInfo(name = "birthdate")
    @TypeConverters(Converters.class)
    private LocalDate birthdate;
    @ColumnInfo(name = "email")
    private String email;
    @ColumnInfo(name = "address")
    private String address;
    @ColumnInfo(name = "cityId")
    private long cityId;
    @ColumnInfo(name = "password")
    private String password;
    @ColumnInfo(name = "phoneNumber")
    private String phoneNumber;
    @ColumnInfo(name = "pictureFileName")
    private String pictureFileName;

    // The minimum and maximum age of the user:
    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 80;

    public User() {
        this.name = "";
        this.surname = "";
        this.gender = "";
        this.birthdate = null;
        this.email = "";
        this.address = "";
        this.password = "";
        this.phoneNumber = "";
        this.pictureFileName = "";
    }

    /**
     * Checks if the connected user is an admin.
     * @return True if the connected user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        return this.phoneNumber.equals(Constants.ADMIN_PHONE_NUMBER);
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCityId() {
        return cityId;
    }

    public void setCityId(long cityId) {
        this.cityId = cityId;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCityName(Context context) {
        final AppDatabase db = AppDatabase.getInstance(context);
        return db.cityDao().getNameById(this.cityId);
    }

    public void setCityByName(Context context, String cityName) {
        final AppDatabase db = AppDatabase.getInstance(context);
        this.cityId = db.cityDao().getIdByName(cityName);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPictureFileName() {
        return pictureFileName;
    }

    public void setPictureFileName(String pictureFileName) {
        this.pictureFileName = pictureFileName;
    }

    public Result<Void, String> deleteUser(Context context) {
        // Delete the user's image from the storage:
        final Result<Void, String> deleteResult = Util.deleteImage(context, this.pictureFileName);
        if (deleteResult.isErr())
            Log.e("User delete", deleteResult.getError());

        // Delete the user from the database:
        final AppDatabase db = AppDatabase.getInstance(context);
        db.userDao().delete(this);
        return Result.success(null);
    }

    public static int getMinAge() {
        return MIN_AGE;
    }

    public static int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    public boolean equals(Object o) {
        // Check if it's the same object:
        if (this == o) return true;

        // Check if it's not the same class or null:
        if (o == null || this.getClass() != o.getClass()) return false;

        // Two users can't have the same ID, so it's enough to check the ID only:
        return this.userId == ((User) o).userId;
    }

    @Override
    public int hashCode() {
        // Hash every field:
        return Objects.hash(
                name,
                surname,
                gender,
                birthdate,
                email,
                address,
                cityId,
                password,
                phoneNumber,
                pictureFileName
        );
    }
}
