package com.example.finalproject.fragments.branch;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.adapters.online.OnlineEmployeeAdapter;
import com.example.finalproject.database.online.CloudFunctionsHandler;
import com.example.finalproject.database.online.collections.Application;
import com.example.finalproject.database.online.collections.Branch;
import com.example.finalproject.database.online.collections.Employee;
import com.example.finalproject.database.online.collections.User;
import com.example.finalproject.util.EmployeeActions;
import com.example.finalproject.util.EmployeeStatus;
import com.example.finalproject.util.Result;
import com.example.finalproject.util.WrapperLinearLayoutManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EmployeesFragment extends Fragment implements EmployeeActions {
    // The current user connected to the app:
    private final User currentUser;

    // The current branch that is being displayed:
    private Branch currentBranch;

    // The text view that tells the user if the branch is currently opened or closed:
    private TextView tvCurrentOpenness;

    // The count down timer that makes the openness text view change in real time:
    private CountDownTimer opennessTimer;

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

    public EmployeesFragment(User currentUser, Branch currentBranch) {
        this.currentUser = currentUser;
        this.currentBranch = currentBranch;
        this.functionsHandler = CloudFunctionsHandler.getInstance();
    }

    public void setCurrentBranch(Branch branch) {
        this.currentBranch = branch;
        if (this.fragmentInitialized())
            this.loadInfoFromBranch();
    }

    private boolean fragmentInitialized() {
        // Since all views are initialized at once, checking only one is enough to know the state
        // of all of them:
        return this.tvCurrentOpenness != null;
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

        // Initialize the recycler view adapter:
        this.initAdapter();

        // Load the info from the branch:
        this.loadInfoFromBranch();

        // If the status was already set, refresh it:
        if (this.employeeStatus != null)
            this.setEmployeeStatus(this.employeeStatus);
        else
            this.setEmployeeStatus(EmployeeStatus.UNEMPLOYED);

        // Initialize the count down timer to change the greeting:
        this.initOpennessTimer();

        return parent;
    }

    private void initOpennessTimer() {
        // Get the next hour in milliseconds:
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        final int nextChangeTime = this.isBranchOpen() ? this.currentBranch.getClosingTime() : this.currentBranch.getOpeningTime();

        // Set the message and color:
        final @StringRes int nextTxt = this.isBranchOpen() ? R.string.act_branch_closed_msg : R.string.act_branch_opened_msg;
        final @ColorInt int nextColor = this.isBranchOpen() ? Color.RED : Color.GREEN;

        // Set the time in the calendar:
        calendar.set(Calendar.HOUR_OF_DAY, nextChangeTime / 60);
        calendar.set(Calendar.MINUTE, nextChangeTime % 60);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the next change in minutes is before the current time, it is tomorrow:
        if (calendar.getTimeInMillis() < System.currentTimeMillis())
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        // Set the count down timer:
        final long TICK_INTERVAL = 1000 * 60 * 5;
        this.opennessTimer = new CountDownTimer(calendar.getTimeInMillis() - System.currentTimeMillis(), TICK_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                final long secondsUntilFinished = millisUntilFinished / 1000;
                Log.i(
                        TAG,
                        String.format(
                                "Changing openness to %b in %d hours, %d minutes and %d seconds (at %d:%02d)",
                                !isBranchOpen(),
                                secondsUntilFinished / (60 * 60),
                                (secondsUntilFinished / 60) % 60,
                                secondsUntilFinished % 60,
                                nextChangeTime / 60,
                                nextChangeTime % 60
                        )
                );
            }

            @Override
            public void onFinish() {
                tvCurrentOpenness.setText(nextTxt);
                tvCurrentOpenness.setTextColor(nextColor);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel the timer:
        if (this.opennessTimer != null)
            this.opennessTimer.cancel();
    }

    private void applyToBranch() {
        // Hide the apply button and show the progress bar:
        this.pbLoading.setVisibility(View.VISIBLE);
        this.btnApply.setVisibility(View.GONE);

        // Get a reference to the database:
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new application object:
        final Application application = new Application(this.currentUser);

        // Create an empty application document with its ID equal to the current user's ID:
        DocumentReference applicationRef = this.currentBranch
                .getReference()
                .collection("applications")
                .document(this.currentUser.getUid());

        // Make a transaction that applies to the branch:
        db.runTransaction((Transaction.Function<Result<Void, String>>) transaction -> {
            // Check if the application already exists:
            final DocumentSnapshot applicationSnap = transaction.get(applicationRef);
            if (applicationSnap.exists()) {
                return Result.failure("The application was already made");
            }

            // If the application is new, create the document and increment the pending applications
            // value:
            else {
                transaction.set(applicationRef, application);
                transaction.update(currentBranch.getReference(), Branch.PENDING_APPLICATIONS, FieldValue.increment(1));
                return Result.success(null);
            }
        }).addOnSuccessListener(result -> {
            // Show the apply button again and hide the progress bar:
            this.btnApply.setVisibility(View.VISIBLE);
            this.pbLoading.setVisibility(View.GONE);

            // If the result is successful:
            if (result.isOk())
                Toast.makeText(requireContext(), "Successfully applied to the branch!", Toast.LENGTH_SHORT).show();
                // If not:
            else
                Toast.makeText(requireContext(), result.getError(), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            // Show the apply button again and hide the progress bar:
            this.btnApply.setVisibility(View.VISIBLE);
            this.pbLoading.setVisibility(View.GONE);

            // Log the error:
            Log.e(TAG, "Failed to apply to branch", e);

            // Alert the user:
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
        });
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
        if (this.employeeStatus == EmployeeStatus.MANAGER && this.adapter.getManagersCount() == 1) {
            // Make the progress bar disappear and the "leave branch" button re-appear:
            this.pbLoading.setVisibility(View.GONE);
            this.btnLeave.setVisibility(View.VISIBLE);

            // Alert the user:
            Toast.makeText(requireContext(), "You can't leave! You're the last manager standing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fire the current user:
        final CloudFunctionsHandler functionsHandler = CloudFunctionsHandler.getInstance();
        functionsHandler.fireUserFromBranch(
                this.currentUser.getUid(),
                this.currentBranch.getBranchId(),
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

    public void setEmployeeStatus(EmployeeStatus status) {
        // Save the status:
        this.employeeStatus = status;

        // Proceed only if the adapter is properly initialized:
        if (this.adapter == null)
            return;

        // Update the adapter:
        this.adapter.setIsManager(status == EmployeeStatus.MANAGER);

        // Show the "leave branch" button if the user is employed at the branch or the "apply to
        // branch" if they aren't:
        final boolean isEmployed = status != EmployeeStatus.UNEMPLOYED;
        this.btnApply.setVisibility(this.currentBranch.isActive() && !isEmployed ? View.VISIBLE : View.GONE);
        this.btnLeave.setVisibility(this.currentBranch.isActive() && isEmployed ? View.VISIBLE : View.GONE);
    }

    private void initAdapter() {
        // Get a reference to the database:
        final FirebaseFirestore dbRef = FirebaseFirestore.getInstance();

        // Create the recyclerView's options:
        final Query query = dbRef
                .collection("branches")
                .document(this.currentBranch.getBranchId())
                .collection("employees")
                .orderBy(Employee.IS_MANAGER, Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Employee> options = new FirestoreRecyclerOptions.Builder<Employee>()
                .setLifecycleOwner(this)
                .setQuery(query, Employee.class)
                .build();

        // Create and set the adapter for the recycler view:
        this.adapter = new OnlineEmployeeAdapter(
                this.employeeStatus == EmployeeStatus.MANAGER,
                true,
                this.currentUser.getUid(),
                this.currentBranch,
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

        // Show the "leave branch" button if the user is employed at the branch or the "apply to
        // branch" if they aren't:
        final boolean isEmployed = this.employeeStatus != EmployeeStatus.UNEMPLOYED;
        this.btnApply.setVisibility(this.currentBranch.isActive() && !isEmployed ? View.VISIBLE : View.GONE);
        this.btnLeave.setVisibility(this.currentBranch.isActive() && isEmployed ? View.VISIBLE : View.GONE);

        // If the branch isn't active, don't allow anyone to promote/demote/fire anyone else:
        this.adapter.setShowEmployeeMenu(this.currentBranch.isActive());
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
}
