package com.project.trackapp.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.trackapp.R;
import com.project.trackapp.model.User;

import org.w3c.dom.Text;

public class UserAdapter extends FirestoreRecyclerAdapter<User,UserAdapter.UserHolder> {
    private  OnUserListener mOnUserListener;

    private Context mCtx;
//    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
//        super(options);
//    }

    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options, OnUserListener mOnUserListener,Context mCtx) {
        super(options);
        this.mOnUserListener = mOnUserListener;
        this.mCtx = mCtx;
    }

    @Override
    protected void onBindViewHolder(@NonNull final UserHolder holder, int position, @NonNull User model) {
        holder.email.setText(model.getEmail());
        if(model.getStatus()) {
            holder.status.setText(mCtx.getString(R.string.online));
           // Color.parseColor("#388E3C");
            holder.status.setTextColor(ContextCompat.getColor(mCtx,R.color.online));
        }
        else
        {
            holder.status.setText(mCtx.getString(R.string.offline));
            holder.status.setTextColor(ContextCompat.getColor(mCtx,R.color.offline));
        }
    }
    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_layout,
                viewGroup,false);

        return new UserHolder(v,mOnUserListener);
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView email;
        TextView status;
        TextView optionButton;
        OnUserListener onUserListener;
        public UserHolder(View itemView,OnUserListener onUserListener){
            super(itemView);
            email = itemView.findViewById(R.id.email_textview);
            status = itemView.findViewById(R.id.status_textview);
            optionButton = itemView.findViewById(R.id.textViewOptions);
            this.onUserListener = onUserListener;
            optionButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onUserListener.onUserClick(getAdapterPosition(),v);
        }
    }

    public interface OnUserListener{
        void onUserClick(int position,View v);
    }

}
