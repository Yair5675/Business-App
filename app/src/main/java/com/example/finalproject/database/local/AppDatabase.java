package com.example.finalproject.database.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.finalproject.database.local.daos.CityDao;
import com.example.finalproject.database.local.daos.UserDao;
import com.example.finalproject.database.local.entities.City;
import com.example.finalproject.database.local.entities.User;

@TypeConverters(Converters.class)
@Database(entities = {User.class, City.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    // The singleton instance of the database:
    private static AppDatabase instance;

    // The DAOs in the database:
    public abstract CityDao cityDao();
    public abstract UserDao userDao();

    // The currently connected user:
    private static User connectedUser;

    public static AppDatabase getInstance(Context context) {
        // Initialize the database if it hadn't been initialized already:
        if (instance == null)
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class, "AppDatabase"
            ).allowMainThreadQueries().build();

        // Return the database instance:
        return instance;
    }

    public static User getConnectedUser() {
        return connectedUser;
    }

    public static void connectUser(User user) {
        connectedUser = user;
    }

    public static boolean isUserLoggedIn() {
        return connectedUser != null;
    }

    public static void disconnect() {
        connectedUser = null;
    }
}

