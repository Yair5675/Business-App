package com.example.finalproject.fragments.branch;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.custom_views.adapters.OnlineEmployeeAdapter;
import com.example.finalproject.database.online.CloudFunctionsHandler;
import com.example.finalproject.database.online.collections.Application;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.EmployeeActions;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.Locale;

public class EmployeesFragment extends Fragment implements EmployeeActions {
    // The current user connected to the app:
    private final User currentUser;

    // The current branch that is being displayed:
    private final Branch currentBranch;

    // The text view that tells the user if the branch is currently opened or closed:
    private TextView tvCurrentOpenness;

    // The text view that shows the working hours of the branch:
    private TextView tvWorkingHours;

    // The text view that shows the branch's address:
    private TextView tvAddress;

    // The text view that appears if the employees recycler view is empty:
    private TextView tvEmployeeNotFound;

    // The adapter for the recycler view:
    private OnlineEmployeeAdapter adapter;

    // The recycler view that shows the branch's employees:
    private RecyclerView rvEmployees;

    // TODO: Implement a search bar to look for a particular employee
    // The button that allows the user to leave the branch:
    private Button btnLeave;

    // The button that allows the user to apply to the branch:
    private Button btnApply;

    // The progress bar in the activity, shown when loading something:
    private ProgressBar pbLoading;

    // The user's status in the branch:
    private EmployeeStatus employeeStatus;

    // Reference to cloud functions:
    private final CloudFunctionsHandler functionsHandler;

    // The listener for the employee's data:
    private ListenerRegistration employeeListener;

    public EmployeesFragment(User currentUser, Branch currentBranch) {
        this.currentUser = currentUser;
        this.currentBranch = currentBranch;
        this.functionsHandler = CloudFunctionsHandler.getInstance();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View parent = inflater.inflate(R.layout.fragment_branch_employees, container, false);

        // Load all views:
        this.loadViews(parent);

        // Hide the "Employee not found" textView:
        this.tvEmployeeNotFound.setVisibility(View.GONE);

        // Initialize layout manager:
        this.rvEmployees.setLayoutManager(new WrapperLinearLayoutManager(requireContext()));

        // Load onClickListeners:
        this.btnApply.setOnClickListener(_v -> this.applyToBranch());
        this.btnLeave.setOnClickListener(_v -> this.leaveBranch());

        // Load the info from the branch:
        this.loadInfoFromBranch();

        // Initialize the recycler view adapter:
        this.initAdapter();

        // Set initial employee status to "employed":
        this.setEmployeeStatus(EmployeeStatus.UNEMPLOYED);

        return parent;
    }

    private void applyToBranch() {
        // Hide the apply button and show the progress bar:
        this.pbLoading.setVisibility(View.VISIBLE);
        this.btnApply.setVisibility(View.GONE);

        // Get a reference to the database:
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new application object:
        final Application application = new Application(
                this.currentUser.getUid(), this.currentUser.getFullName(), this.currentUser.getImagePath()
        );

        // Create an empty notification document with its ID equal to the current user's ID:
        DocumentReference notificationRef = db
                .collection("branches")
                .document(this.currentBranch.getBranchId())
                .collection("applications")
                .document(this.currentUser.getUid());

        // Set the application object in the document:
        notificationRef.set(application, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    // Show the apply button again and hide the progress bar:
                    this.btnApply.setVisibility(View.VISIBLE);
                    this.pbLoading.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // If the task is successful, alert the user:
                        Toast.makeText(requireContext(), "Successfully applied to the branch!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // Log the error:
                        Log.e(TAG, "Failed to apply to branch", task.getException());

                        // Alert the user:
                        Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initListener() {
        // Add the listener:
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.employeeListener = db
                .collection("branches")
                .document(this.currentBranch.getBranchId())
                .collection("employees")
                .document(this.currentUser.getUid())
                // Add a snapshot listener:
                .addSnapshotListener((value, error) -> {
                    // Check the error:
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }

                    // Check if the user isn't employed at the branch:
                    EmployeeStatus status;
                    if (value == null || !value.exists())
                        status = EmployeeStatus.UNEMPLOYED;

                    else {
                        // Convert to employee:
                        final Employee employee = value.toObject(Employee.class);

                        // Check if the employee is a manager:
                        if (employee == null)
                            status = EmployeeStatus.UNEMPLOYED;
                        else if (employee.isManager())
                            status = EmployeeStatus.MANAGER;
                        else
                            status = EmployeeStatus.EMPLOYED;
                    }

                    // Change the status for the activity:
                    this.setEmployeeStatus(status);
                });
    }

    private enum EmployeeStatus {
        MANAGER,
        EMPLOYED,
        UNEMPLOYED
    }

    // Tag for debugging purposes:
    private static final String TAG = "BranchFragmentEmployees";

    private void loadViews(View parent) {
        this.tvCurrentOpenness = parent.findViewById(R.id.fragBranchEmployeesTvCurrentOpenness);
        this.tvWorkingHours = parent.findViewById(R.id.fragBranchEmployeesTvWorkingTimes);
        this.tvAddress = parent.findViewById(R.id.fragBranchEmployeesTvAddress);
        this.rvEmployees = parent.findViewById(R.id.fragBranchEmployeesRvEmployees);
        this.tvEmployeeNotFound = parent.findViewById(R.id.fragBranchEmployeesTvEmployeeNotFound);
        this.btnApply = parent.findViewById(R.id.fragBranchEmployeesBtnApplyToBusiness);
        this.btnLeave = parent.findViewById(R.id.fragBranchEmployeesBtnLeaveBranch);
        this.pbLoading = parent.findViewById(R.id.fragBranchEmployeesPbLoading);
        this.pbLoading.setVisibility(View.GONE);
    }

    private void leaveBranch() {
        // Show the progress bar and make the "leave branch" button disappear:
        this.pbLoading.setVisibility(View.VISIBLE);
        this.btnLeave.setVisibility(View.GONE);

        // Prevent the user from leaving if they are the only employee:
        if (this.adapter.getItemCount() == 1) {
            // Make the progress bar disappear and the "leave branch" button re-appear:
            this.pbLoading.setVisibility(View.GONE);
            this.btnLeave.setVisibility(View.VISIBLE);

            // Alert the user:
            Toast.makeText(requireContext(), "You can't leave! You're the last one standing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fire the current user:
        final CloudFunctionsHandler functionsHandler = CloudFunctionsHandler.getInstance();
        functionsHandler.fireUserFromBranch(
                this.currentUser.getUid(),
                this.currentBranch.getBranchId(),
                this.currentUser.getFullName(),
                this.currentBranch.getCompanyName(),
                () -> {
                    // Make the progress bar disappear:
                    this.pbLoading.setVisibility(View.GONE);

                    // Set the employee status to unemployed (shows apply button automatically):
                    this.setEmployeeStatus(EmployeeStatus.UNEMPLOYED);

                    // Alert the user:
                    Toast.makeText(requireContext(), "Left branch successfully!", Toast.LENGTH_SHORT).show();
                },
                e -> {
                    // Make the progress bar disappear and the "leave branch" button re-appear:
                    this.pbLoading.setVisibility(View.GONE);
                    this.btnLeave.setVisibility(View.VISIBLE);

                    // Log the error:
                    Log.e(TAG, "Error leaving branch", e);

                    // Alert the user:
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void setEmployeeStatus(EmployeeStatus status) {
        // Save the status:
        this.employeeStatus = status;

        // Update the adapter:
        this.adapter.setIsManager(status == EmployeeStatus.MANAGER);

        // Show the "leave branch" button if the user is employed at the branch or the "apply to
        // branch" if they aren't:
        final boolean isEmployed = status != EmployeeStatus.UNEMPLOYED;
        this.btnApply.setVisibility(isEmployed ? View.GONE : View.VISIBLE);
        this.btnLeave.setVisibility(isEmployed ? View.VISIBLE : View.GONE);
    }

    private void initAdapter() {
        // Get a reference to the database:
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

        // Create the recyclerView's options:
        final Query query = dbRef
                .collection("branches")
                .document(this.currentBranch.getBranchId())
                .collection("employees");
        FirestoreRecyclerOptions<Employee> options = new FirestoreRecyclerOptions.Builder<Employee>()
                .setLifecycleOwner(this)
                .setQuery(query, Employee.class)
                .build();

        // Create and set the adapter for the recycler view:
        this.adapter = new OnlineEmployeeAdapter(
                this.employeeStatus == EmployeeStatus.MANAGER,
                this.currentUser.getUid(),
                requireContext(),
                () -> {
                    // Show the "Employee not found" textView and hide the recyclerView:
                    this.tvEmployeeNotFound.setVisibility(View.VISIBLE);
                    this.rvEmployees.setVisibility(View.GONE);
                },
                () -> {
                    // Show the recyclerView and hide the "Employee not found" textView:
                    this.tvEmployeeNotFound.setVisibility(View.GONE);
                    this.rvEmployees.setVisibility(View.VISIBLE);
                },
                this, options
        );
        this.rvEmployees.setAdapter(this.adapter);
    }

    private void loadInfoFromBranch() {
        // Set the current openness:
        if (isBranchOpen()) {
            this.tvCurrentOpenness.setText(R.string.act_branch_opened_msg);
            this.tvCurrentOpenness.setTextColor(Color.GREEN);
        }
        else {
            this.tvCurrentOpenness.setText(R.string.act_branch_closed_msg);
            this.tvCurrentOpenness.setTextColor(Color.RED);
        }

        // Set the opening and closing time:
        final int openHour = this.currentBranch.getOpeningTime() / 60,
                openMinute = this.currentBranch.getOpeningTime() % 60,
                closedHour = this.currentBranch.getClosingTime() / 60,
                closedMinute = this.currentBranch.getClosingTime() % 60;
        this.tvWorkingHours.setText(String.format(
                Locale.getDefault(), "Opened during: %d:%02d - %d:%02d",
                openHour, openMinute, closedHour, closedMinute
        ));

        // Set the address:
        this.tvAddress.setText(this.currentBranch.getFullAddress());
    }

    private boolean isBranchOpen() {
        final Calendar calendar = Calendar.getInstance();
        final int currentMinutes = 60 * calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE);
        return this.currentBranch.getClosingTime() > currentMinutes &&
                currentMinutes >= this.currentBranch.getOpeningTime();
    }

    @Override
    public void promote(Employee employee) {
        // Show the progress bar and hide the "Leave branch":
        pbLoading.setVisibility(View.VISIBLE);
        btnLeave.setVisibility(View.GONE);

        // Call the cloud function:
        this.functionsHandler.setEmployeeStatus(
                employee.getUid(),
                currentBranch.getBranchId(),
                true,
                employee.getFullName(),
                currentBranch.getCompanyName(),
                () -> {
                    // Show the "Leave branch" button and hide the progress bar:
                    pbLoading.setVisibility(View.GONE);
                    btnLeave.setVisibility(View.VISIBLE);
                },
                e -> {
                    // Show the "Leave branch" button and hide the progress bar:
                    pbLoading.setVisibility(View.GONE);
                    btnLeave.setVisibility(View.VISIBLE);

                    // Log the error:
                    Log.e(TAG, "Error promoting employee", e);

                    // Alert the user:
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    public void demote(Employee employee) {
        // Show the progress bar and hide the "Leave branch":
        pbLoading.setVisibility(View.VISIBLE);
        btnLeave.setVisibility(View.GONE);

        // Call the cloud function:
        this.functionsHandler.setEmployeeStatus(
                employee.getUid(),
                currentBranch.getBranchId(),
                false,
                employee.getFullName(),
                currentBranch.getCompanyName(),
                () -> {
                    // Show the "Leave branch" button and hide the progress bar:
                    pbLoading.setVisibility(View.GONE);
                    btnLeave.setVisibility(View.VISIBLE);
                },
                e -> {
                    // Show the "Leave branch" button and hide the progress bar:
                    pbLoading.setVisibility(View.GONE);
                    btnLeave.setVisibility(View.VISIBLE);

                    // Log the error:
                    Log.e(TAG, "Error demoting employee", e);

                    // Alert the user:
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    public void fire(Employee employee) {
        // Show the progress bar and hide the "Leave branch":
        pbLoading.setVisibility(View.VISIBLE);
        btnLeave.setVisibility(View.GONE);

        // Fire the employee:
        this.functionsHandler.fireUserFromBranch(
                employee.getUid(),
                currentBranch.getBranchId(),
                employee.getFullName(),
                currentBranch.getCompanyName(),
                () -> {
                    // Show the "Leave branch" button and hide the progress bar:
                    pbLoading.setVisibility(View.GONE);
                    btnLeave.setVisibility(View.VISIBLE);
                },
                e -> {
                    // Show the "Leave branch" button and hide the progress bar:
                    pbLoading.setVisibility(View.GONE);
                    btnLeave.setVisibility(View.VISIBLE);

                    // Log the error:
                    Log.e(TAG, "Error firing employee", e);

                    // Alert the user:
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Initialize the listener:
        this.initListener();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unbind the listener:
        if (this.employeeListener != null) {
            this.employeeListener.remove();
            this.employeeListener = null;
        }
    }
}
