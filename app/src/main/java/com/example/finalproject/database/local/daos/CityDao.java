package com.example.finalproject.database.local.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.finalproject.database.local.entities.City;

import java.util.List;

@Dao
public interface CityDao {
    @Query("SELECT * FROM TblCities")
    List<City> getAll();

    @Query("SELECT COUNT(*) FROM Tblcities")
    int getCitiesCount();

    /**
     * Searches the database for a city whose name matches the given name without considering
     * capitalization.
     * @param cityName The name of the city searched in the database.
     * @return A city object whose name matches the given name if one was found, if not null.
     */
    @Query("SELECT * FROM TblCities WHERE LOWER(cityName) = LOWER(:cityName) LIMIT 1")
    City getCityByNameIgnoreCase(String cityName);

    @Query("SELECT cityId FROM Tblcities WHERE cityName = :cityName LIMIT 1")
    long getIdByName(String cityName);

    @Query("SELECT cityName FROM Tblcities WHERE cityId = :cityId")
    String getNameById(long cityId);

    @Insert
    long insert(City city);

    @Delete
    void delete(City city);

    @Update
    void update(City city);
}
