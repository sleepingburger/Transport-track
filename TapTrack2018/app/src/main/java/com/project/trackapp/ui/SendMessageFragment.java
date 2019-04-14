package com.project.trackapp.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.trackapp.R;
import com.project.trackapp.model.Customer;
import com.project.trackapp.model.Messages;
import com.project.trackapp.model.User;

import java.util.ArrayList;

public class SendMessageFragment extends Fragment implements View.OnClickListener{
    FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    CollectionReference messagesRef;
    private static final String TAG = "SendMessageFragment";
    private EditText firstname,lastname,trackno,email,address,mobile,description;

    private String fname="",lname="",trackNumber="",mEmail="",mAddress="",mMobile="";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        Toast.makeText(this.getActivity(), "starting", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_customer,container,false);

        init(view);
        view.findViewById(R.id.add).setOnClickListener(this);
        view.findViewById(R.id.close).setOnClickListener(this);
        return view;
    }

    private void init(View view) {
        firstname = (EditText)view.findViewById(R.id.firstname);
        lastname = (EditText)view.findViewById(R.id.lastname);
        trackno = (EditText)view.findViewById(R.id.track_no);
        email = (EditText)view.findViewById(R.id.email);
        mobile = (EditText)view.findViewById(R.id.mobile);
        address = (EditText)view.findViewById(R.id.address);
        description = (EditText)view.findViewById(R.id.description);
        firstname.setText("");
        lastname.setText("");
        trackno.setText("");
        email.setText("");
        mobile.setText("");
        address.setText("");
    }

    public static SendMessageFragment newInstance(){
        return new SendMessageFragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add:{
                addCustomer();
                break;
            }
            case R.id.close:{
                getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                break;
            }
        }
    }

    private void addCustomer() {
        if(validateFields()){
            DocumentReference customer = mDb
                    .collection(getString(R.string.collection_customer))
                    .document();

            String desc = description.getText().toString().equals("")?"No Description":description.getText().toString();
            Customer c = new Customer(Integer.parseInt(trackNumber),fname,lname,mAddress,Long.parseLong(mMobile),mEmail,desc);

            customer.set(c).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: SUCCESSFULLY ADDED THE CUSTOMER");
                    }
                }
            });
        }
        else
        {
            Toast.makeText(getActivity().getApplicationContext(), "All fields are required! ", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateFields() {
        fname = firstname.getText().toString();
        lname = lastname.getText().toString();
        trackNumber = trackno.getText().toString();
        mEmail = email.getText().toString();
        mMobile = mobile.getText().toString();
        mAddress = address.getText().toString();
        if(!fname.equals("")
            && !lname.equals("")
            && !trackNumber.equals("")
            && !mAddress.equals("")
            && !mEmail.equals("")
            && !mMobile.equals("")){
            return true;
        }
        return false;
    }
}
