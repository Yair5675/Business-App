package com.example.finalproject.database.online;

import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CloudFunctionsHandler {
    // TODO: Change some functions here (and in the python files) because of the new branches
    //  deletion system

    // TODO: I am not sure but maybe roles aren't deleted when deleting a branch? Anyhow check that
    //  and fix it will you lad?
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

    public void deleteBranch(
            String branchId,
            Runnable onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Call the function with the branch ID:
        final Map<String, String> data = new HashMap<>();
        data.put("branchId", branchId);
        this.functions
                .getHttpsCallable("delete_branch")
                .call(data)
                .addOnSuccessListener(_r -> onSuccessListener.run())
                .addOnFailureListener(onFailureListener);
    }

    public void deleteAllShifts(
            LocalDate date,
            String branchId,
            Runnable onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Load the parameters:
        final Map<String, Object> data = new HashMap<>();
        data.put("day", date.getDayOfMonth());
        data.put("month", date.getMonthValue());
        data.put("year", date.getYear());
        data.put("branchId", branchId);

        // Call the function:
        this.functions
                .getHttpsCallable("delete_all_shifts")
                .call(data)
                .addOnSuccessListener(_r -> onSuccessListener.run())
                .addOnFailureListener(onFailureListener);
    }

    public void resolveApplication(
            String uid,
            Branch branch,
            boolean accepted,
            boolean isManager,
            Runnable onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Get the user from the database:
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Convert the document to a user object:
                    final User user = documentSnapshot.toObject(User.class);
                    if (user == null) {
                        onFailureListener.onFailure(new Exception("Couldn't load user"));
                        return;
                    }

                    if (accepted) {
                        // Jsonify parameters and send to the cloud function:
                        final Map<String, Object> data = new HashMap<>();
                        data.put("user", user.jsonifyUser());
                        data.put("branch", branch.jsonifyBranch());
                        data.put("manager", isManager);

                        this.functions
                                .getHttpsCallable("accept_application")
                                .call(data)
                                .addOnSuccessListener(_r -> onSuccessListener.run())
                                .addOnFailureListener(onFailureListener);
                    }
                    else {
                        // If they are not accepted, just delete the application from the database:
                        db
                                .collection("branches")
                                .document(branch.getBranchId())
                                .collection("applications")
                                .document(uid)
                                .delete()
                                .addOnSuccessListener(_r -> onSuccessListener.run())
                                .addOnFailureListener(onFailureListener);
                    }

                })
                .addOnFailureListener(onFailureListener);
    }
}
