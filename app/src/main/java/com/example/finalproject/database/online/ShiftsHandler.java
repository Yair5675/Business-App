package com.example.finalproject.database.online;

import com.example.finalproject.database.online.collections.Shift;
import com.example.finalproject.database.online.collections.Worker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ShiftsHandler {
    // The ID of the branch:
    private final String branchId;

    // A reference to the database:
    private final FirebaseFirestore db;

    // A hashmap connecting a single shift object to a list of workers:
    private final HashMap<Shift, ArrayList<Worker>> shiftEmployees;

    // Tag for debugging purposes:
    private static final String TAG = "ShiftsHandler";

    public ShiftsHandler(String branchId) {
        // Load parameters:
        this.branchId = branchId;

        // Initialize the map:
        this.shiftEmployees = new HashMap<>();

        // Initialize the database reference:
        this.db = FirebaseFirestore.getInstance();
    }

    public void addNewShift(Shift shift) {
        this.shiftEmployees.put(shift, new ArrayList<>());
    }

    public void deleteShift(Shift shift) {
        this.shiftEmployees.remove(shift);
    }

    public void loadShiftsFrom(Date date, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        // Get the shifts:
        this.getShifts(
                date,
                shifts -> {
                    // Add to the hashmap:
                    for (Shift shift : shifts)
                        this.addShiftAndWorkers(shift, onSuccessListener, onFailureListener);
                },
                onFailureListener
        );
    }

    private void addShiftAndWorkers(
            Shift shift, OnSuccessListener<Void> onSuccessListener,
            OnFailureListener onFailureListener
    ) {
        // Get all workers from the shift:
        this.db.collection(String.format(
                "branches/%s/shifts/%s/workers",
                this.branchId, shift.getShiftId()
        )).get().addOnSuccessListener(documents -> {
                    // Convert to workers:
                    ArrayList<Worker> workers = new ArrayList<>(documents.size());
                    for (QueryDocumentSnapshot document : documents) {
                        workers.add(document.toObject(Worker.class));
                    }

                    // Add to the hashmap:
                    this.shiftEmployees.put(shift, workers);

                    // Call the callback:
                    onSuccessListener.onSuccess(null);
                }).addOnFailureListener(onFailureListener);
    }

    private void getShifts(Date date, OnSuccessListener<List<Shift>> onSuccessListener, OnFailureListener onFailureListener) {
        this.db.
                collection(String.format("branches/%s/shifts", this.branchId))
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocuments -> {
                    // Get all shifts:
                    List<Shift> shifts = new ArrayList<>(queryDocuments.size());
                    for (QueryDocumentSnapshot document : queryDocuments)
                        shifts.add(document.toObject(Shift.class));
                    onSuccessListener.onSuccess(shifts);
                }).addOnFailureListener(onFailureListener);
    }
}
