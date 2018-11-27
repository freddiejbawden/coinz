package com.example.s1636469.coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class SignInActivity extends Activity {

    private FirebaseAuth mAuth;
    private String TAG = "SignIn";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        setUpListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setUpListeners() {
        Button signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((TextView) findViewById(R.id.username)).getText().toString();
                String password = ((TextView) findViewById(R.id.password)).getText().toString();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // we have been logged in!
                            Log.d(TAG, "signed in");
                            Intent i = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(i);
                            // pass to another intent
                        } else {
                            Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        Button signUpButton = (Button) findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((TextView) findViewById(R.id.username)).getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Make sure to enter an email address!",
                            Toast.LENGTH_SHORT).show();
                }
                String password = ((TextView) findViewById(R.id.password)).getText().toString();
                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Make sure to enter a password",
                            Toast.LENGTH_SHORT).show();
                }
                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    Log.d(TAG, "created user");
                                    Intent i = new Intent(SignInActivity.this, SetUpAccountActivity.class);
                                    i.putExtra("email",email);
                                    startActivity(i);
                                } else {
                                    Exception e = task.getException();
                                    if (e instanceof FirebaseAuthUserCollisionException) {
                                        Log.d(TAG, "User collision on sign up");
                                        Toast.makeText(getApplicationContext(),
                                                "This email already has an account associated with it," +
                                                        " please use a another email address!",
                                                Toast.LENGTH_LONG).show();
                                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                        Log.d(TAG,"User did not enter a correctly formed email or password");
                                        Toast.makeText(getApplicationContext(),
                                                "Your email or password is malformed, your " +
                                                        "password should be at least 6 characters long",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Log.w(TAG, "A uncaught exception was thrown",e);
                                    }
                                }
                            }
                        });
            }
        });

    }

}
