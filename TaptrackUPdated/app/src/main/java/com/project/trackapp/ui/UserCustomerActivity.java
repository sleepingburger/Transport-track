package com.project.trackapp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.trackapp.R;
import com.project.trackapp.model.Customer;
import com.project.trackapp.model.User;
import com.project.trackapp.util.CustomerAdapter;
import com.project.trackapp.util.GoOnline;
import com.project.trackapp.util.UserAdapter;

import java.util.ArrayList;

public class UserCustomerActivity extends AppCompatActivity implements CustomerAdapter.OnCustomerListener {


    CollectionReference customerList;
    FirebaseFirestore mDb = FirebaseFirestore.getInstance();


    RecyclerView mCustomerListRecyclerView;

    ArrayList<User> userList = new ArrayList<>();

    CollectionReference userRef;

    Customer customer;

    User user;
    RecyclerView listOnlineRecyclerView;
    private static final String TAG = "UserCustomerActivity";
    ArrayList<Customer> mCustomersList = new ArrayList<>();
    private Context mCtx;
    private CustomerAdapter adapter;
    private UserAdapter mUserAdapter;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onPostResume() {
        super.onPostResume();
        new GoOnline().pushOnline(TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pushOnline();

        setContentView(R.layout.activity_user_customer);

        customerList = mDb.collection(getString(R.string.collection_customer));
        userRef = mDb.collection("ActiveUsers");
        mCustomerListRecyclerView = findViewById(R.id.listcustomer_recyclerview);
        mCtx = this;
        setUpCustomers();
    }

    public void pushOnline() {
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run(){
                new GoOnline().pushOnline(TAG);
            }
        },1000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        new GoOnline().pushOnline(TAG);
    }


    public void setUpCustomers() {
        new GoOnline().pushOnline(TAG);

        mCustomersList.clear();
        CollectionReference clients = mDb.collection("ActiveUsers")
                .document(FirebaseAuth.getInstance().getUid())
                .collection(getString(R.string.collection_customer));

        Query query = clients.orderBy("timestamp", Query.Direction.ASCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                        Log.d(TAG, "onComplete: CUSSTOMER HAS BEEN ADDED");
                        Customer c = task.getResult().getDocuments().get(i).toObject(Customer.class);
                        mCustomersList.add(c);
                    }
                    initCustomerrListRecyclerView();
                }
            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
        goOffline();
    }




    private void goOffline() {
        FirebaseFirestore.getInstance().collection("ActiveUsers").document(FirebaseAuth.getInstance().getUid())
                .update("status",false).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: OFFLINE!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UserCustomerActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initCustomerrListRecyclerView() {
        adapter = new CustomerAdapter(mCustomersList,this,this);
        mCustomerListRecyclerView.setAdapter(adapter);
        mCustomerListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onCustomerClick(final int position, View v) {
        switch (v.getId()){
            case R.id.textViewOptions:{
                customer = mCustomersList.get(position);
                PopupMenu popup = new PopupMenu(this,v);
                //inflating menu from xml resource
                popup.inflate(R.menu.user_customer_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.message_button: {
                                openSms();
                                return true;
                            }
                            case R.id.remove_button:{
                                removeUser(position);
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


    private void removeUser(final int pos) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want remove and set this as done?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DocumentReference doc = userRef.document(FirebaseAuth.getInstance().getUid())
                                .collection(getString(R.string.collection_customer))
                                .document(customer.getUid());

                        doc.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    customer.setStatus("DONE");
                                    customerList.document(customer.getUid()).set(customer).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                mCustomersList.remove(pos);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(UserCustomerActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
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
}
