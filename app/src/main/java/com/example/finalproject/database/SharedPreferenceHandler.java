package com.example.finalproject.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Result;

public class SharedPreferenceHandler {
    // The shared preference object and its editor:
    private final SharedPreferences sp;
    private final SharedPreferences.Editor editor;

    // Since the class follows the singleton pattern, only one instance is created:
    private static SharedPreferenceHandler instance;

    private SharedPreferenceHandler(Context context) {
        // Initialize the shared preference and the editor:
        this.sp = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        this.editor = this.sp.edit();
    }

    public static SharedPreferenceHandler getInstance(Context context) {
        if (instance == null)
            instance = new SharedPreferenceHandler(context);
        return instance;
    }

    public void remove(String key) {
        if (key != null)
            this.editor.remove(key).commit();
    }

    public void putLong(String key, long value) {
        // Check that the key isn't null:
        if (key != null)
            this.editor.putLong(key, value).commit();
    }

    public Result<Long, String> getLong(String key) {
        // Check that the key isn't null:
        if (key == null)
            return Result.failure("Invalid null key");
        // Check that the key exists:
        else if (!this.sp.contains(key))
            return Result.failure("Unknown key: " + key);

        // Get the long but make sure to check that it is actually a long value:
        try {
            return Result.success(this.sp.getLong(key, 0));
        } catch (ClassCastException e) {
            return Result.failure(e.getMessage());
        }
    }
}
