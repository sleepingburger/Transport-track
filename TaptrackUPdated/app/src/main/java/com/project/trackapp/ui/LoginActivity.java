package com.project.trackapp.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.trackapp.R;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "LoginActivity";
    private EditText email,password;

    //widgets
    private ProgressBar mProgressBar;

    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        hideSoftKeyboard();
        checkFirebaseAuth();
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.register_button).setOnClickListener(this);

        mProgressBar = findViewById(R.id.progressBar);
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void checkFirebaseAuth() {
        Log.e(TAG, "checkFirebaseAuth: checking" );

        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if(currentUser != null){
                    Log.e(TAG, "onAuthStateChanged: validated user state" + currentUser.getUid());
                    Toast.makeText(LoginActivity.this, "Successfully Login" + currentUser
                            .getEmail().substring(0,currentUser.getEmail().indexOf("@")), Toast.LENGTH_SHORT).show();

                    if(!currentUser.getEmail().substring(0,currentUser.getEmail().indexOf("@")).equals("admin")){
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    else{//if admin
                        Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mFirebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mFirebaseAuthListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mFirebaseAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.login_button:{
                validateUser();

                break;
            }

            case R.id.register_button:{
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                break;
            }
        }
    }

    private void validateUser() {
        if(!isEmpty(email.getText().toString())
                && !isEmpty(password.getText().toString())){
            Log.e(TAG, "validateUser: Proceeding" + email.toString());

            //progressbar
            showDialog();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(),
                    password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //checkFirebaseAuth();
                            //FirebaseAuth.getInstance().addAuthStateListener(mFirebaseAuthListener);

                            //progressbar
                            hideDialog();

                            Log.e(TAG, "onComplete: Login Successfull");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //progressbar
                    hideDialog();

                    Toast.makeText(LoginActivity.this, "Authentication Failed! ", Toast.LENGTH_SHORT).show();

                }
            });
        }
        else
        {
            Toast.makeText(this, "Empty field is not allowed", Toast.LENGTH_LONG).show();
        }
    }
}
