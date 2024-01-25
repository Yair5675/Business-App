package com.example.finalproject.database.local.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.finalproject.database.local.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM TblUsers")
    List<User> getAll();

    @Query("SELECT * FROM TblUsers WHERE userId == :id")
    User getUserById(long id);

    @Query("SELECT * FROM TblUsers WHERE " +
            // Filter the users by name/surname:
            "name LIKE :name || '%' OR surname LIKE :name || '%' OR name || ' ' || surname LIKE :name || '%' " +
            // Sort them (if the sorting option isn't equal to those three options, input won't be
            // sorted):
            "ORDER BY " +
            "CASE WHEN :sortingOption = 'NAME' THEN name END, " + // Sort by name
            "CASE WHEN :sortingOption = 'SURNAME' THEN surname END, " + // Sort by surname
            "CASE WHEN :sortingOption = 'AGE' THEN birthdate END DESC") // Sort by birthdate (youngest first)
    List<User> getUsersSortedAndFiltered(String name, String sortingOption);

    @Query("SELECT * FROM TblUsers WHERE email == :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM TblUsers WHERE phoneNumber == :phoneNumber LIMIT 1")
    User getUserByPhone(String phoneNumber);

    @Query("SELECT * FROM TblUsers WHERE" +
            " phoneNumber == :phoneNumber AND" +
            " email == :email AND" +
            " password == :password LIMIT 1")
    User findUserByCredentials(String phoneNumber, String email, String password);

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}
