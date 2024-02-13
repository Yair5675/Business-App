package com.example.finalproject.custom_views;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public abstract class OnlineAdapter<T, VH extends RecyclerView.ViewHolder> extends FirestoreRecyclerAdapter<T, VH> {
    // The context of the recycler view:
    protected final Context context;

    // A callback that's run every time the adapter is empty:
    private final Runnable onEmptyCallback;

    // A callback that's run every time the data changes and the recycle has at least one item:
    private final Runnable onNotEmptyCallback;

    public OnlineAdapter(
            Context context,
            Runnable onEmptyCallback,
            Runnable onNotEmptyCallback,
            @NonNull FirestoreRecyclerOptions<T> options
    ) {
        super(options);
        this.context = context;
        this.onEmptyCallback = onEmptyCallback;
        this.onNotEmptyCallback = onNotEmptyCallback;
    }

    public boolean isEmpty() {
        return this.getItemCount() == 0;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();

        // Check if the recycler view is empty:
        if (this.isEmpty())
            this.onEmptyCallback.run();
        else
            this.onNotEmptyCallback.run();
    }
}
