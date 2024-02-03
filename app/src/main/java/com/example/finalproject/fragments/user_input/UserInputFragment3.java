package com.example.finalproject.fragments.user_input;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.finalproject.database.online.OnlineDatabase;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.Permissions;
import com.example.finalproject.util.Util;

import java.io.IOException;


public class UserInputFragment3 extends Fragment implements View.OnClickListener {
    // A reference to the database:
    private final OnlineDatabase db;

    // A reference to the user whose details are being changed:
    private final User user;

    // The imageView which displays the image of the user:
    private ImageView imgUser;

    // The URI and bitmap of the user's image:
    private Uri uriPhoto;
    private Bitmap bitmapPhoto;

    // The method through which the photo was taken (camera or gallery):
    private PhotoTakenFrom photoTakenFrom;

    // Tag for debugging purposes:
    private static final String TAG = "UserInputFragment3";


    private enum PhotoTakenFrom {
        CAMERA,
        GALLERY
    }

    private ActivityResultLauncher<Intent> imageReceiver;

    public UserInputFragment3(@Nullable User connectedUser) {
        this.user = connectedUser;
        this.db = OnlineDatabase.getInstance();
    }

    public void loadInputsFromUser(User user) {
        // Make sure no other bitmap was saved:
        if (this.bitmapPhoto == null)
            // Get the bitmap from the database and set it:
            this.db.getUserImage(user, userImage -> {
                // Save the user's image:
                bitmapPhoto = userImage;

                // Set the photo:
                Util.setCircularImage(requireContext(), imgUser, userImage);
            }, e -> {
                Log.e(TAG, "Failed to download image", e);
                Toast.makeText(requireContext(), "Couldn't load image", Toast.LENGTH_SHORT).show();
            });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the XML file of the third input fragment:
        final View parent = inflater.inflate(R.layout.fragment_user_input_3, container, false);

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
        this.imgUser = parent.findViewById(R.id.fragUserInput3ImgUser);
        if (this.bitmapPhoto != null)
            Util.setCircularImage(this.requireContext(), this.imgUser, this.bitmapPhoto);

        // Set the two upload buttons' OnClickListener:
        parent.findViewById(R.id.fragUserInput3BtnUploadCamera).setOnClickListener(this);
        parent.findViewById(R.id.fragUserInput3BtnUploadGallery).setOnClickListener(this);

        // Check if a connected user was given:
        if (this.user != null)
            this.loadInputsFromUser(this.user);

        return parent;
    }


    @Override
    public void onClick(View view) {
        // Identify the view:
        final int ID = view.getId();

        if (ID == R.id.fragUserInput3BtnUploadCamera) {
            // Check for permission to use the camera:
            if (Permissions.checkPermissions(requireContext(), Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                // If the permission is granted, take a picture from the camera:
                takePicFromCamera();
            else
                // If not, ask for permission to open the camera:
                Permissions.requestPermissions(requireActivity(), Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        }
        else if (ID == R.id.fragUserInput3BtnUploadGallery) {
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
            Log.e("UserInputFragment3 - loadBitmapFromUri", e.toString());
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
            // Set it as the image of the user if it was loaded properly:
            Util.setCircularImage(this.requireContext(), this.imgUser, this.bitmapPhoto);
        }
        else
            Toast.makeText(requireContext(), "Could not load the picture", Toast.LENGTH_SHORT).show();

    }

    private void handleCameraPhoto() {
        // Load bitmap right away because the URI is already set:
        if (this.loadBitmapFromUri()) {
            // Set it as the image of the user if it was loaded properly:
            Util.setCircularImage(this.requireContext(), this.imgUser, this.bitmapPhoto);
        }
        else
            Toast.makeText(requireContext(), "Could not load the picture", Toast.LENGTH_SHORT).show();
    }

    /**
     * Checks that the user actually picked an image and shows a toast message in case they did not
     * @param context The context of the activity that holds the fragment. Will be used for toast
     *                messages.
     * @return True if the user picked an image, False otherwise.
     */
    public boolean areInputsValid(Context context) {
        // Check that the bitmap file is saved:
        final boolean isImageLoaded = this.bitmapPhoto != null;
        if (!isImageLoaded)
            Toast.makeText(context, "Please choose a picture", Toast.LENGTH_SHORT).show();

        return isImageLoaded;
    }

    public Bitmap getBitmapPhoto() {
        return this.bitmapPhoto;
    }
}
