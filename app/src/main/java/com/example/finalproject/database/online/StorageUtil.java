package com.example.finalproject.database.online;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.finalproject.database.online.collections.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

public class StorageUtil {
    public static String getStorageImagePath(String uid) {
        return String.format(Locale.getDefault(), "images/%s/%d", uid, System.currentTimeMillis());
    }

    /**
     * Loads the image of the user into an image view.
     * @param context The context calling the load, necessary for Glide.
     * @param user The user whose image will be loaded into the image view.
     * @param imageView The image view that the user's image will be loaded into.
     * @param errorImg The ID of a drawable that will be shown if an error occurred while retrieving
     *                 the user's image.
     */
    public static void loadUserImgFromStorage(
            Context context,
            User user,
            ImageView imageView,
            @DrawableRes int errorImg
    ) {
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Glide.with(context)
                .load(storageRef.child(user.getImagePath()))
                .apply(
                        new RequestOptions()
                                .error(errorImg)
                                .circleCrop()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                .into(imageView);
    }
}
