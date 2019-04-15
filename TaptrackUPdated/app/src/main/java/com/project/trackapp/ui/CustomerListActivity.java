package com.project.trackapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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


    CollectionReference userRef;

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
        Query query = userRef.orderBy("status", Query.Direction.DESCENDING);
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
    public void onCustomerClick(int position, View v) {
        switch (v.getId()){
            case R.id.textViewOptions:{
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
                            case R.id.delete_button:{
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
    private void assignToUser() {
        mCustomerListRecyclerView.setVisibility(View.GONE);
        listOnlineRecyclerView.setVisibility(View.VISIBLE);
    }
    @Override
    public void onUserClick(int position, View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Assign this customer to \n"+ position)
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(CustomerListActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        assign();
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

    private void assign() {



        startActivity(new Intent(CustomerListActivity.this,AdminActivity.class));
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
