package com.project.trackapp.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.project.trackapp.R;
import com.project.trackapp.model.Customer;

import java.util.ArrayList;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {


    private OnCustomerListener mCustomerListener;

    private ArrayList<Customer> mCustomers;

    private Context mCtx;

    public CustomerAdapter(ArrayList<Customer> mCustomers,Context mCtx,OnCustomerListener mCustomerListener) {
        this.mCustomers = mCustomers;
        this.mCtx = mCtx;
        this.mCustomerListener = mCustomerListener;
    }

    @NonNull
    @Override
    public CustomerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.customer_layout, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view,mCustomerListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerAdapter.ViewHolder viewHolder, int i) {
        viewHolder.name.setText(mCustomers.get(i).getFirstName() + " " +mCustomers.get(i).getLastName());
        viewHolder.mobileno.setText(mCustomers.get(i).getMobile()+"");
        viewHolder.address.setText(mCustomers.get(i).getAddress());
        viewHolder.trackno.setText(mCustomers.get(i).getDeliveryNumber() + "");
        viewHolder.status.setText(mCustomers.get(i).getStatus());

        if(mCustomers.get(i).getStatus().equals("Pending")) {
            //viewHolder.status.setTextColor(ContextCompat.getColor(mCtx, R.color.offline));
            viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(mCtx,R.color.pending));
        }
        else if(mCustomers.get(i).getStatus().contains("Ongoing")){
            //viewHolder.status.setTextColor(ContextCompat.getColor(mCtx,R.color.online));
            viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(mCtx,R.color.ongoing));
        }
        else {
            viewHolder.itemView.setBackgroundColor(ContextCompat.getColor(mCtx,R.color.done));
        }
    }

    @Override
    public int getItemCount() {
        return mCustomers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView name;
        TextView status;
        TextView trackno;
        TextView mobileno;
        TextView address;
        TextView option;
        OnCustomerListener onCustomerListener;
        public ViewHolder(View itemView,OnCustomerListener onCustomerListener) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            status = itemView.findViewById(R.id.status);
            trackno = itemView.findViewById(R.id.track_no);
            mobileno = itemView.findViewById(R.id.mobile);
            address = itemView.findViewById(R.id.address);
            option = itemView.findViewById(R.id.textViewOptions);
            this.onCustomerListener = onCustomerListener;
            option.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCustomerListener.onCustomerClick(getAdapterPosition(),v);
        }
    }
    public interface OnCustomerListener{
        void onCustomerClick(int position,View v);
    }

//    /**
//     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
//     * FirestoreRecyclerOptions} for configuration options.
//     *
//     * @param options
//     */
//    private Context mCtx;
//    public CustomerAdapter(@NonNull FirestoreRecyclerOptions<Customer> options, Context mCtx) {
//        super(options);
//        this.mCtx = mCtx;
//    }
//
//    @Override
//    protected void onBindViewHolder(@NonNull CustomerHolder holder, int position, @NonNull Customer model) {
//        holder.name.setText(model.getFirstName() + " " +model.getLastName());
//        holder.mobileno.setText(model.getMobile()+"");
//        holder.address.setText(model.getAddress());
//        holder.trackno.setText(model.getDeliveryNumber());
//        holder.status.setText("Pending");
//    }
//
//    @NonNull
//    @Override
//    public CustomerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.customer_layout,
//                viewGroup,false);
//
//        return new CustomerHolder(v);
//    }
//
//    class CustomerHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//
//        TextView name;
//        TextView status;
//        TextView trackno;
//        TextView mobileno;
//        TextView address;
//
//
//        public CustomerHolder(@NonNull View itemView) {
//            super(itemView);
//            name = itemView.findViewById(R.id.name);
//            status = itemView.findViewById(R.id.status);
//            trackno = itemView.findViewById(R.id.track_no);
//            mobileno = itemView.findViewById(R.id.mobile);
//            address = itemView.findViewById(R.id.address);
//        }
//
//        @Override
//        public void onClick(View v) {
//
//        }
//    }

}
