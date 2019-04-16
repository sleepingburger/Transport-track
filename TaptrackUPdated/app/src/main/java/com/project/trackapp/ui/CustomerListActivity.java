package com.project.trackapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.trackapp.R;
import com.project.trackapp.model.Customer;
import com.project.trackapp.model.User;
import com.project.trackapp.util.CustomerAdapter;
import com.project.trackapp.util.UserAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomerListActivity extends AppCompatActivity implements CustomerAdapter.OnCustomerListener,UserAdapter.OnUserListener {


    CollectionReference customerList;
    FirebaseFirestore mDb = FirebaseFirestore.getInstance();


    RecyclerView mCustomerListRecyclerView;

    ArrayList<User> userList = new ArrayList<>();

    CollectionReference userRef;

    Customer customer;

    User user;
    RecyclerView listOnlineRecyclerView;
    private static final String TAG = "CustomerListActivity";
    ArrayList<Customer> mCustomersList = new ArrayList<>();
    private Context mCtx;
    private CustomerAdapter adapter;
    private UserAdapter mUserAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        customerList = mDb
                .collection(getString(R.string.collection_customer));

        userRef = mDb
                .collection("ActiveUsers");


        mCustomerListRecyclerView = findViewById(R.id.listcustomer_recyclerview);

        setUpCustomers();
        firebaseRefSetup();
    }

    private void firebaseRefSetup() {
        Log.d(TAG, "firebaseRefSetup: " +userList.size());
        Query query = userRef.orderBy("status", Query.Direction.DESCENDING);

        try {
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        userList.addAll(task.getResult().toObjects(User.class));
                        if(userList.size() == 0){
                            startActivity(new Intent(CustomerListActivity.this,AdminActivity.class));
                        }
                    }
                }
            });
        }catch (NullPointerException e){
            Toast.makeText(this, "Error in this activity", Toast.LENGTH_SHORT).show();
        }



        //Query query = userRef.orderBy("email", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class).build();
        mUserAdapter = new UserAdapter(options,this, this,false);
        listOnlineRecyclerView = findViewById(R.id.listonline_recyclerview);
        listOnlineRecyclerView.setHasFixedSize(true);
        listOnlineRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listOnlineRecyclerView.setAdapter(mUserAdapter);
    }




    public void setUpCustomers() {
        mCustomersList.clear();
        CollectionReference clients = mDb
                .collection(getString(R.string.collection_customer));
        clients.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(int i = 0; i < task.getResult().getDocuments().size(); i++){
                        Log.d(TAG, "onComplete: CUSSTOMER HAS BEEN ADDED");
                        Customer c = task.getResult().getDocuments().get(i).toObject(Customer.class);
                        mCustomersList.add(c);
                    }
                    initCustomerrListRecyclerView();
                }
            }
        });
    }


    private void initCustomerrListRecyclerView() {
        adapter = new CustomerAdapter(mCustomersList,this,this);
        mCustomerListRecyclerView.setAdapter(adapter);
        mCustomerListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCustomerClick(final int position, View v) {
        switch (v.getId()){
            case R.id.textViewOptions:{
                customer = mCustomersList.get(position);
                PopupMenu popup = new PopupMenu(this,v);
                //inflating menu from xml resource
                popup.inflate(R.menu.customer_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.assign_button: {
                                assignToUser();
                                return true;
                            }
                            case R.id.send_message_button:{

                                openSms();

                                return true;
                            }
                            case R.id.delete_button:{
                                deleteCustomer(position);
                                return true;
                            }
                        }
                        return true;
                    }
                });
                //displaying the popup
                popup.show();
            }
        }
    }

    private void deleteCustomer(final int pos) {
               final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want delete?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        customerList.document(customer.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    mCustomersList.remove(pos);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(CustomerListActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    private void openSms() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Open the SMS app?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.setData(Uri.parse("sms:+63" + customer.getMobile()));
                        startActivity(sendIntent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void assignToUser() {
        mCustomerListRecyclerView.setVisibility(View.GONE);
        listOnlineRecyclerView.setVisibility(View.VISIBLE);
    }
    @Override
    public void onUserClick(int position, View v) {
        user = userList.get(position);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Assign this customer to "+ user.getEmail())
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        assign(user.getUid());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void assign(final String userUID) {
        customer.setStatus("Ongoing assign to "+ user.getEmail().substring(0,user.getEmail().indexOf("@")));
        customerList.document(customer.getUid()).set(customer).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    userRef.document(userUID).collection(getString(R.string.collection_customer))
                            .document(customer.getUid()).set(customer).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CustomerListActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(CustomerListActivity.this,AdminActivity.class));
                            }
                        }
                    });


                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserAdapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mUserAdapter.stopListening();
    }


}
