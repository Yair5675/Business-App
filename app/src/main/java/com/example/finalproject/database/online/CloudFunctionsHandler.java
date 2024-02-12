package com.example.finalproject.database.online;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class CloudFunctionsHandler {
    // A reference to firebase functions:
    private final FirebaseFunctions functions;

    // The region of the functions:
    private static final String REGION = "me-west1";

    // The only instance of the class:
    private static CloudFunctionsHandler instance;

    public CloudFunctionsHandler() {
        this.functions = FirebaseFunctions.getInstance(REGION);
    }

    public static CloudFunctionsHandler getInstance() {
        if (instance == null)
            instance = new CloudFunctionsHandler();
        return instance;
    }

    public void fireUserFromBranch(
            String uid,
            String branchId,
            Runnable onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Load the parameters into a hashmap:
        final Map<String, String> data = new HashMap<>();
        data.put("uid", uid);
        data.put("branchId", branchId);

        // Call the function with the parameters:
        functions
                .getHttpsCallable("fire_user_from_branch")
                .call(data)
                // The function shouldn't return anything if it succeeds:
                .addOnSuccessListener(_r -> onSuccessListener.run())
                .addOnFailureListener(onFailureListener);
    }

    public void setEmployeeStatus(
            String uid,
            String branchId,
            boolean isManager,
            Runnable onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Load the parameters into a hashmap:
        final Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("branchId", branchId);
        data.put("manager", isManager);

        // Call the function with the parameters:
        functions
                .getHttpsCallable("set_employee_status")
                .call(data)
                // The function shouldn't return anything if it succeeds:
                .addOnSuccessListener(_r -> onSuccessListener.run())
                .addOnFailureListener(onFailureListener);
    }
}
