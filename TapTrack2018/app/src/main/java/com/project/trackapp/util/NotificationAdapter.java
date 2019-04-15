package com.project.trackapp.util;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.trackapp.model.Messages;

public class NotificationAdapter extends FirestoreRecyclerAdapter<Messages,NotificationAdapter.NotificationHolder> {


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public NotificationAdapter(@NonNull FirestoreRecyclerOptions<Messages> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NotificationAdapter.NotificationHolder holder, int position, @NonNull Messages model) {

    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    class NotificationHolder extends RecyclerView.ViewHolder {

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
