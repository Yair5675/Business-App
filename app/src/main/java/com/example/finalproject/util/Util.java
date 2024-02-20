package com.example.finalproject.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

public class Util {
    /**
     * A null-safe getter for an editText input.
     * @param et An editText whose input will be returned.
     * @return The input inside the given editText. If the editText is null or the editable inside
     *         it is null, an empty string is returned.
     */
    public static @NonNull String getTextFromEt(@Nullable EditText et) {
        if (et == null)
            return "";

        Editable editable;
        if ((editable = et.getText()) != null)
            return editable.toString();
        else
            return "";
    }

    /**
     * Validates the input of an input field, and if validation failed the error that the validator
     * returned is set for the input layout.
     * @param inputLayout The layout for the input field, used to set the error if one was returned.
     * @param inputField The input field itself, used to get the input value.
     * @param validator A validator function that will validate the input field.
     */
    public static void validateAndSetError(@NonNull TextInputLayout inputLayout,
                                           @NonNull EditText inputField,
                                           @NonNull Function<String, Result<Void, String>> validator)
    {
        // Get the user input:
        final String input = getTextFromEt(inputField);

        // Validate using the validator and apply the error if one is returned:
        final Result<Void, String> result = validator.apply(input);
        inputLayout.setError(result.isOk() ? null : result.getError());
    }

    /**
     * Closes the keyboard.
     * @param context Context of the activity where the keyboard should be closed.
     * @param view The view which was focused on when the keyboard opened.
     */
    public static void closeKeyboard(Context context, View view) {
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Opens the keyboard.
     * @param context Context of the activity where the keyboard should be opened.
     * @param view The view that the keyboard should focus on.
     */
    public static void openKeyboard(Context context, View view) {
        view.requestFocus();
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    /**
     * Opens an image file from the external files directory and returns it in Bitmap format.
     * @param context Context that is required when accessing the external files directory.
     * @param imageName The name of the image file in question.
     * @return If the file exists, it is returned in bitmap format wrapped with the Result class.
     *         If not, a FileNotFoundException is returned wrapped with the Result class instead.
     */
    public static Result<Bitmap, FileNotFoundException> getImage(Context context, String imageName)
    {
        try {
            // Open the PNG file using FileInputStream:
            final File externalStorage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            final File dir = new File(externalStorage, Constants.IMAGES_FOLDER_NAME);
            final File dest = new File(dir, imageName);

            FileInputStream fileInputStream = new FileInputStream(dest);

            // Decode the input stream into a Bitmap:
            return Result.success(BitmapFactory.decodeStream(fileInputStream));

        } catch (FileNotFoundException e) {
            return Result.failure(e);
        }
    }

    /**
     * Deletes an image from the external files directory.
     * @param context Context that is required when accessing the external files directory.
     * @param imageName The name of the image file which will be deleted.
     * @return If the operation went well, an empty Result will be returned. Otherwise, a Result
     *         with an error message will be returned.
     */
    public static Result<Void, String> deleteImage(Context context, String imageName) {
        // Get the picture in storage:
        final File externalStorage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File dir = new File(externalStorage, Constants.IMAGES_FOLDER_NAME);

        // Make sure the directory exists:
        if (!dir.exists())
            return Result.failure("External storage dir doesn't exist");

        // Make sure the file itself exists:
        final File pic = new File(dir, imageName);
        if (!pic.exists())
            return Result.failure("Profile pic file doesn't exist");

        // Delete the file:
        if (pic.delete())
            return Result.success(null);
        else
            return Result.failure("Error occurred while deleting profile pic file");
    }

    public static String fixNamingCapitalization(String name) {
        if (name == null || name.isEmpty())
            return "";
        // Separate into words:
        String[] words = name.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                words[i] = words[i].toLowerCase();
                final char first = (char) (words[i].charAt(0) - 32);
                words[i] = first + words[i].substring(1);
            }
        }
        return String.join(" ", words);
    }

    public static void setCircularImage(Context context, ImageView imageHolder, @DrawableRes int id) {
        final RequestOptions ro = new RequestOptions().circleCrop();
        Glide.with(context).load(id).apply(ro).into(imageHolder);
    }

    public static void setCircularImage(Context context, ImageView imageHolder, Bitmap image) {
        final RequestOptions ro = new RequestOptions().circleCrop();
        Glide.with(context).load(image).apply(ro).into(imageHolder);
    }

    public static byte[] toByteArray(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * Creates a Date object from three integers: year, month and day of the month.
     * @param year The year.
     * @param month The month INDEX (meaning, January is 0).
     * @param day The day of the month.
     * @return A Timestamp object of this date.
     */
    public static Date getDateFromInts(int year, int month, int day) {
        // Create a calendar and set it to the given date (at the start of the day):
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Create the date and return it:
        return Date.from(calendar.toInstant());
    }

    /**
     * Rotates a bitmap clockwise by a certain amount of degrees.
     * @param bitmap A bitmap before rotation.
     * @param degrees The rotation that will be applied to the bitmap clockwise.
     * @return A new bitmap similar to the received bitmap but rotated clockwise.
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        final Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotated;
    }


    // Zoom level for google maps:
    public enum Zoom {
        WORLD(1),
        CONTINENT(5),
        CITY(10),
        STREETS(15),
        BUILDINGS(20)
        ;

        public final int level;

        Zoom(int level) {
            this.level = level;
        }
    }
}
