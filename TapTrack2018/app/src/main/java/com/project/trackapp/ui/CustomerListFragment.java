package com.project.trackapp.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.project.trackapp.R;
import com.project.trackapp.model.Customer;
import com.project.trackapp.model.User;
import com.project.trackapp.model.UserLocation;
import com.project.trackapp.util.CustomerAdapter;
import com.project.trackapp.util.UserAdapter;

import java.util.ArrayList;

public class CustomerListFragment extends Fragment {

    private static final String TAG = "CustomerListFragment";
    public static CustomerListFragment newInstance(){
        return new CustomerListFragment();
    }

    CollectionReference customerList;
    FirebaseFirestore mDb = FirebaseFirestore.getInstance();


    RecyclerView mCustomerListRecyclerView;


    ArrayList<Customer> mCustomersList = new ArrayList<>();
    private Context mCtx;
    private CustomerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: customerLIST FRAGMENT");
        customerList = mDb
                .collection(getString(R.string.collection_customer));

        //firebaseRefSetup(v)


        if (mCustomersList.size() == 0) { // make sure the list doesn't duplicate by navigating back
            if (getArguments() != null) {
                final ArrayList<Customer> customers = getArguments().getParcelableArrayList(getString(R.string.intent_customer_list));
                mCustomersList.addAll(customers);
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_customer_list,container,false);
        mCustomerListRecyclerView = v.findViewById(R.id.listcustomer_recyclerview);

        initCustomerrListRecyclerView();

        return  v;
    }
    private void initCustomerrListRecyclerView() {
        adapter = new CustomerAdapter(mCustomersList);
        mCustomerListRecyclerView.setAdapter(adapter);
        mCustomerListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
    /*

    private void firebaseRefSetup(View view) {
        Query query = customerList.orderBy("timestamp", Query.Direction.ASCENDING);
        //Query query = userRef.orderBy("email", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Customer> options = new FirestoreRecyclerOptions.Builder<Customer>()
                .setQuery(query, Customer.class).build();
        adapter = new CustomerAdapter(options,mCtx);
        RecyclerView recyclerView = view.findViewById(R.id.listcustomer_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mCtx));

        recyclerView.setAdapter(adapter);
    }*/

}
