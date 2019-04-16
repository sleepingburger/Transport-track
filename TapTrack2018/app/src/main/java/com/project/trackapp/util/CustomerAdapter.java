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



    private ArrayList<Customer> mCustomers = new ArrayList<>();


    public CustomerAdapter(ArrayList<Customer> mCustomers) {
        this.mCustomers = mCustomers;
    }

    @NonNull
    @Override
    public CustomerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.customer_layout, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerAdapter.ViewHolder viewHolder, int i) {
        viewHolder.name.setText(mCustomers.get(i).getFirstName() + " " +mCustomers.get(i).getLastName());
        viewHolder.mobileno.setText(mCustomers.get(i).getMobile()+"");
        viewHolder.address.setText(mCustomers.get(i).getAddress());
        viewHolder.trackno.setText(mCustomers.get(i).getDeliveryNumber());
        viewHolder.status.setText("Pending");
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder
    {

        TextView name;
        TextView status;
        TextView trackno;
        TextView mobileno;
        TextView address;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            status = itemView.findViewById(R.id.status);
            trackno = itemView.findViewById(R.id.track_no);
            mobileno = itemView.findViewById(R.id.mobile);
            address = itemView.findViewById(R.id.address);
        }

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
