package com.project.trackapp.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.trackapp.R;
import com.project.trackapp.model.Messages;
import com.project.trackapp.model.User;

import java.util.ArrayList;

public class SendMessageFragment extends Fragment implements View.OnClickListener {
    FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    CollectionReference messagesRef;
    private static final String TAG = "SendMessageFragment";
    private ArrayList<User> mUserList = new ArrayList<>();
    private String sSubject,sBody;
    private User whichUser;
    private EditText subject,body;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
        Toast.makeText(this.getActivity(), "starting", Toast.LENGTH_SHORT).show();

        if(getArguments() != null){
            mUserList = getArguments().getParcelableArrayList(getString(R.string.intent_user_list));
            Log.d(TAG, "onCreate: getting arguments");
            for(int i = 0; i < mUserList.size(); i++){
                if(i == getArguments().getInt("pos")){
                    Log.d(TAG, "onCreate: "+ i);
                    whichUser = mUserList.get(i);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_message,container,false);
        subject = (EditText)view.findViewById(R.id.title);
        body = (EditText)view.findViewById(R.id.body);
        subject.setText("");
        body.setText("");
        view.findViewById(R.id.send_message).setOnClickListener(this);
        return view;
    }

    public static SendMessageFragment newInstance(){
        return new SendMessageFragment();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_message:{
                sendNotification();
            }
        }
    }

    private void sendNotification() {
        if(isNotEmpty()) {
            sSubject = subject.getText().equals("")?"No Subject":subject.getText().toString();
            sBody = body.getText().toString();
            messagesRef = mDb.collection(getString(R.string.collection_messages));
            DocumentReference docRef = messagesRef
                    .document(whichUser.getUid());

            CollectionReference allMessagesRef = docRef
                    .collection(getString(R.string.collection_all_notification));

            Messages newMessage = new Messages(whichUser,sBody,sSubject,whichUser.getUid(),null);

            allMessagesRef.add(newMessage).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(), "Message sent!", Toast.LENGTH_SHORT).show();
                        goBack();
                    }
                }
            });
        }
        else
        {
            Toast.makeText(getParentFragment().getContext(), "Empty field is not allowed", Toast.LENGTH_SHORT).show();
        }
    }

    private void goBack() {
        subject.setText("");
        body.setText("");
    }

    private boolean isNotEmpty() {
        if(subject.getText().equals("")){
            return false;
        }
        return true;
    }
}
