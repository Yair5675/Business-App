package com.example.finalproject.database.local.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "TblCities")
public class City {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cityId")
    private long cityId;

    @ColumnInfo(name = "cityName")
    private String cityName;

    public City(String cityName) {
        this.cityName = cityName;
    }

    public long getCityId() {
        return cityId;
    }

    public void setCityId(long cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @NonNull
    @Override
    public String toString() {
        return this.cityName;
    }
}
