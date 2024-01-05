package com.example.finalproject.fragments.input;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;
import com.example.finalproject.database.AppDatabase;
import com.example.finalproject.database.entities.User;
import com.example.finalproject.util.Constants;
import com.example.finalproject.util.Permissions;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class InputFragment3 extends Fragment implements View.OnClickListener {

    // The imageView which displays the image of the user:
    private ImageView imgUser;

    // The URI and bitmap of the user's image:
    private Uri uriPhoto;
    private Bitmap bitmapPhoto;

    // The file-name of the user's image:
    private String imgFileName;

    // The method through which the photo was taken (camera or gallery):
    private PhotoTakenFrom photoTakenFrom;

    // The phone number of the user, will be involved in creating a unique file name:
    private final String userPhoneNumber;

    private enum PhotoTakenFrom {
        CAMERA,
        GALLERY
    }

    private ActivityResultLauncher<Intent> imageReceiver;

    public InputFragment3(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public void loadInputsFromUser(User user) {
        // Check that there is not image loaded:
        if (this.imgFileName == null) {
            // Load the image bitmap from the user:
            final Result<Bitmap, FileNotFoundException> result = Util.getImage(
                    requireContext(),
                    user.getPictureFileName()
            );
            if (result.isOk()) {
                this.bitmapPhoto = result.getValue();
                Util.setCircularImage(this.requireContext(), this.imgUser, this.bitmapPhoto);
                this.imgFileName = user.getPictureFileName();

            } else {
                Log.e("InputFragment3 load user", result.getError().toString());
                Toast.makeText(requireActivity(), "Couldn't load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the XML file of the third input fragment:
        final View parent = inflater.inflate(R.layout.fragment_input_3, container, false);

        // Initialize the image receiver:
         this.imageReceiver = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK)
                        if (this.photoTakenFrom == PhotoTakenFrom.CAMERA)
                            handleCameraPhoto();
                        else
                            handleGalleryPhoto(result);
                }
        );

        // Load the user's imageView:
        this.imgUser = parent.findViewById(R.id.fragInput3ImgUser);
        if (this.bitmapPhoto != null)
            Util.setCircularImage(this.requireContext(), this.imgUser, this.bitmapPhoto);

        // Set the two upload buttons' OnClickListener:
        parent.findViewById(R.id.fragInput3BtnUploadCamera).setOnClickListener(this);
        parent.findViewById(R.id.fragInput3BtnUploadGallery).setOnClickListener(this);

        // If a user is connected, load the inputs from them:
        if (AppDatabase.isUserLoggedIn())
            this.loadInputsFromUser(AppDatabase.getConnectedUser());

        return parent;
    }


    @Override
    public void onClick(View view) {
        // Identify the view:
        final int ID = view.getId();

        if (ID == R.id.fragInput3BtnUploadCamera) {
            // Check for permission to use the camera:
            if (Permissions.checkPermissions(requireContext(), Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                // If the permission is granted, take a picture from the camera:
                takePicFromCamera();
            else
                // If not, ask for permission to open the camera:
                Permissions.requestPermissions(requireActivity(), Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        }
        else if (ID == R.id.fragInput3BtnUploadGallery) {
            // Check for permission to open the gallery:
            if (Permissions.checkPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                // If the permission is granted, take a picture from the gallery:
                takePicFromGallery();
            else
                // If not, ask for permission to open the gallery:
                Permissions.requestPermissions(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void takePicFromGallery() {
        // Signal we want a picture from the gallery:
        this.photoTakenFrom = PhotoTakenFrom.GALLERY;

        // Activate the gallery:
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imageReceiver.launch(intent);
    }

    private void takePicFromCamera() {
        // Signal we want a picture from the camera:
        this.photoTakenFrom = PhotoTakenFrom.CAMERA;

        // Activate the camera:
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        uriPhoto = requireContext()
                .getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriPhoto);
        imageReceiver.launch(intent);
    }

    /**
     * Creates a bitmap object from the 'uriPhoto' field and saves it in the 'bitmapPhoto' field.
     * @return True if no error was raised, false otherwise.
     */
    private boolean loadBitmapFromUri() {
        try {
            this.bitmapPhoto = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), this.uriPhoto);
            return true;
        } catch (IOException e) {
            Log.e("InputFragment3 - loadBitmapFromUri", e.toString());
            return false;
        }
    }

    private void handleGalleryPhoto(ActivityResult result) {
        // Load the URI:
        Intent data;
        if ((data = result.getData()) == null) {
            Toast.makeText(requireContext(), "Could not load the picture", Toast.LENGTH_SHORT).show();
            return;
        }
        this.uriPhoto = data.getData();

        // Load bitmap from the URI:
        if (this.loadBitmapFromUri()) {
            // Try to save the bitmap:
            if (this.saveUserImage())
                // Set it as the image of the user if it was saved properly:
                Util.setCircularImage(this.requireContext(), this.imgUser, this.bitmapPhoto);
            else
                Toast.makeText(requireContext(), "Could not load the picture", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(requireContext(), "Could not load the picture", Toast.LENGTH_SHORT).show();

    }

    private void handleCameraPhoto() {
        // Load bitmap right away because the URI is already set:
        if (this.loadBitmapFromUri()) {
            // Try to save the bitmap:
            if (this.saveUserImage())
                // Set it as the image of the user if it was saved properly:
                Util.setCircularImage(this.requireContext(), this.imgUser, this.bitmapPhoto);
            else
                Toast.makeText(requireContext(), "Could not load the picture", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(requireContext(), "Could not load the picture", Toast.LENGTH_SHORT).show();
    }

    /**
     * Saves the user's chosen picture in the images directory for later use.
     * @return True if the image was saved successfully, false otherwise.
     */
    private boolean saveUserImage() {
        // If another image was saved beforehand, delete it:
        if (this.imgFileName != null)
            Util.deleteImage(requireContext(), this.imgFileName);

        // Define a unique filename according to the format specified by the Constants class:
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS");
        this.imgFileName = Constants.IMAGE_FILE_NAME_FORMAT
                .replace(Constants.IMAGE_FILE_NAME_PHONE_TAG, userPhoneNumber)
                .replace(Constants.IMAGE_FILE_NAME_TIME_TAG, LocalDateTime.now().format(formatter));

        // Create a directory for the photos:
        final File externalStorage = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        final File dir = new File(externalStorage, Constants.IMAGES_FOLDER_NAME);

        // Create it only if it doesn't exist already:
        if (!dir.exists()) {
            final boolean createdDir = dir.mkdirs();
            Log.i("Created directory", Boolean.toString(createdDir));
        }

        Log.i("path to directory", dir.getAbsolutePath());

        // Save the image in the directory:
        final File dest = new File(dir, this.imgFileName);
        try {
            // Prepare the image and the output stream to save the file:
            final FileOutputStream outputStream = new FileOutputStream(dest);
            this.bitmapPhoto.compress(Bitmap.CompressFormat.PNG, 90, outputStream);

            // Save the file and close the output stream:
            outputStream.close();

            return true;
        } catch (IOException e) {
            Log.e("InputFragment3 - saveUserImage", e.toString());

            return false;
        }
    }

    /**
     * Checks all inputs and their validity. If some inputs are invalid, the function will present
     * an error to the user (if one wasn't presented already).
     * @param context The context of the activity that holds the fragment. Will be used for toast
     *                messages.
     *           img0585570004-2023-12-06-15-45-06-618.png
     * @return True if all inputs are valid, False otherwise.
     */
    public boolean areInputsValid(Context context) {
        // Check that the file name is loaded, and create a toast message if not:
        final boolean isImageLoaded = this.imgFileName != null;
        if (!isImageLoaded)
            Toast.makeText(context, "Please choose a picture", Toast.LENGTH_SHORT).show();

        return isImageLoaded;
    }

    /**
     * Returns the name of the file that the user chose as their profile picture.
     * Pay attention this getter should only be called after calling the 'areInputsValid' function.
     * @return The name of the file that the user chose as their profile picture.
     */
    public String getImgFileName() {
        return this.imgFileName;
    }
}
