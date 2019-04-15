package com.project.trackapp.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.trackapp.R;
import com.project.trackapp.UserClient;
import com.project.trackapp.model.Customer;
import com.project.trackapp.model.User;
import com.project.trackapp.model.UserLocation;
import com.project.trackapp.util.UserAdapter;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener, UserAdapter.OnUserListener {
    public static final String TAG = "AdminActivity";

    private ListenerRegistration userLocationEventListner,getUserLocationEventListner;
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();

    //user define firestore recyler adapter
    private UserAdapter adapter;



    ArrayList<Customer> mCustomerList = new ArrayList<>();
    int userPosition = 0;
    //firebase references
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userRef = db.collection("ActiveUsers");

    //view
    private RecyclerView listOnline;
    private RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(userLocationEventListner != null){
            userLocationEventListner.remove();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        init();

        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Welcome Admin");
        setSupportActionBar(toolbar);


        firebaseRefSetup();

        findViewById(R.id.viewmap_button).setOnClickListener(this);
        findViewById(R.id.addcustomer_button).setOnClickListener(this);
        findViewById(R.id.viewcustomer_button).setOnClickListener(this);

        mUserList.clear();





        saveAdmin();


    }



    private void saveAdmin() {
        User admin = new User("Admin",
                true,
                FirebaseAuth.getInstance().getUid());
        ((UserClient)(getApplication())).setUser(admin);
    }


    private void firebaseRefSetup() {
        Query query = userRef.orderBy("status", Query.Direction.DESCENDING);
        //Query query = userRef.orderBy("email", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class).build();

        adapter = new UserAdapter(options,this, this,true);

        RecyclerView recyclerView = findViewById(R.id.listonline_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);
    }

    protected void onResume() {
        super.onResume();
        getUsers();

    }

    private void getUsers() {
        mUserList.clear();
        mUserLocations.clear();
        userLocationEventListner = userRef.orderBy("status", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "onEvent: Listen failed.", e);
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    mUserList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d(TAG, "onEvent: ");
                        User user = doc.toObject(User.class);
                        mUserList.add(user);
                        Log.d(TAG, "onEvent: USERSSADAS"+user.getEmail());
                        if(doc.getBoolean("status")) {
                            getUserLocation(user);
                        }
                    }
                }
            }
        });
    }
    private void getUserLocation(User user) {
        DocumentReference locationRef = db.collection(getString(R.string.collection_user_locations))
                .document(user.getUid());
        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    UserLocation uL = task.getResult().toObject(UserLocation.class);
                    if(uL != null){
                        Log.d(TAG, "onComplete: "+uL.getGeo_point().toString());
                        mUserLocations.add(uL);
                    }
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private void init() {
        listOnline = (RecyclerView) findViewById(R.id.listonline_recyclerview);
        listOnline.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        listOnline.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout_button: {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.viewmap_button: {
                if(mUserLocations != null) {
                    inflateMap();
                }
                break;
            }
            case R.id.addcustomer_button:{
                inflateAddCustomer();
                break;
            }
            case R.id.viewcustomer_button:{
                inflateCustomerList();
                break;
            }
        }
    }

    private void inflateAddCustomer() {
        SendMessageFragment fragment = SendMessageFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.create_message_container, fragment, getString(R.string.add_customer));
        transaction.addToBackStack(getString(R.string.add_customer));
        transaction.commit();
    }

    private void inflateCustomerList() {

        Intent intent = new Intent(AdminActivity.this,CustomerListActivity.class);
        startActivity(intent);
    }

    private void inflateMap() {
        hideSoftKeyboard();
        Intent intent = new Intent(AdminActivity.this,AdminMapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_userlist), mUserList);
        Log.d(TAG, "inflateMap: " + mUserLocations.size());
        bundle.putParcelableArrayList(getString(R.string.intent_user_locations), mUserLocations);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    private void inflateNewMessage(int userPosition){
        hideSoftKeyboard();
            Intent intent = new Intent(AdminActivity.this,ChatRoomActivity.class);
            intent.putExtra("uid",mUserList.get(userPosition).getUid());
        Log.d(TAG, "inflateNewMessage: uid" + mUserList.get(userPosition).getEmail());
            startActivity(intent);
            mUserList.clear();
    }
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onUserClick(final int position, View v) {
        Toast.makeText(this, "Pos user" + position, Toast.LENGTH_SHORT).show();
        switch (v.getId()){
            case R.id.textViewOptions:{
                PopupMenu popup = new PopupMenu(this,v);
                //inflating menu from xml resource
                popup.inflate(R.menu.user_option_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.send_message: {
                                userPosition = position;
                                inflateNewMessage(userPosition);
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
}
