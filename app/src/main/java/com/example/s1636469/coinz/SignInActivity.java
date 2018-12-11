/*
 *  SignInActivity
 *
 *  Handles signing in existing users and signing up new ones
 *
 */


package com.example.s1636469.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends Activity {

    private FirebaseAuth mAuth;
    private String TAG = "SignIn";
    private ProgressBar progressBar;
    private EditText email_text;
    private EditText password_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_sign_in);

        Log.d(TAG, "Staring main");
        // Get a reference to Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Set up references to UI elements
        progressBar = findViewById(R.id.login_progress);
        email_text = findViewById(R.id.email_edit_text);
        password_text = findViewById(R.id.password_edit_text);

        progressBar.setVisibility(View.INVISIBLE);

        setUpListeners();
    }

    /*
     * check_user
     *
     * After the user has logged in, perform admin tasks on their account; if this the first log in
     * of the day, checking if they have provided as username yet
     *
     */
    private void check_user(String id,String email) {

        //Get reference to Firebase Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference u_ref = database.collection("users").document(id);

        // Get the logged in user's details
        u_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> u_data = documentSnapshot.getData();

                // Check if the user has not completed account set up
                if (!documentSnapshot.exists()) {
                    Intent i = new Intent(SignInActivity.this, SetUpAccountActivity.class);
                    i.putExtra("email",email);
                    startActivity(i);
                    return;
                }

                // Check if this is the first time the user has logged in today
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                DateTime last_log = new DateTime((Date) u_data.get("last_login"));
                DateTime now =  new DateTime(Calendar.getInstance().getTime());

                LocalDate last_log_date = last_log.toLocalDate();
                LocalDate now_date = now.toLocalDate();

                if (last_log_date.compareTo(now_date) != 0) {
                    // date has changed
                    Log.d(TAG, "Date has changed since last login");
                    HashMap<String, Object> to_put = new HashMap<>();
                    to_put.put("last_login",Calendar.getInstance().getTime());
                    to_put.put("collected", new ArrayList<String>());
                    for (String c : Config.currencies) {
                        to_put.put(c,0);
                    }
                    u_ref.set(to_put, SetOptions.merge());
                }
                Intent i = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(i);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Unable to log you in at this time, " +
                        "please try again later",Toast.LENGTH_SHORT).show();
            }
        });
    }


    /*
     *  onSignInPressed
     *
     *  Attempts to log in user using Firebase Authentication
     *
     */
    private void onSignInPressed() {

        progressBar.setVisibility(View.VISIBLE);


        String email = ((TextView) findViewById(R.id.email_edit_text)).getText().toString();
        String password = ((TextView) findViewById(R.id.password_edit_text)).getText().toString();

        if (email.isEmpty()) {
            email_text.setError(getString(R.string.no_email));
            email_text.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (password.isEmpty()) {
            password_text.setError(getString(R.string.no_password));
            password_text.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }


        //Sign in user using provided email and password
        mAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                check_user(authResult.getUser().getUid(), email);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                // Find out why the user could not be logged in and inform user
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Log.d(TAG,"User did not enter a correctly formed email or password");
                    Toast.makeText(getApplicationContext(), R.string.bad_email_password,
                            Toast.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseAuthInvalidUserException){
                    email_text.setError(getString(R.string.no_user_with_account));
                    email_text.requestFocus();
                } else {
                    Log.w(TAG, "A uncaught exception was thrown",e);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }


    /*
     * onSignUpPressed
     *
     *  Attempts to create a new account in Firebase Authentication then continue to account set up
     *
     */
    private void onSignUpPressed() {

        progressBar.setVisibility(View.VISIBLE);

        String email = email_text.getText().toString();
        String password = password_text.getText().toString();
        if (email.isEmpty()) {
            email_text.setError(getString(R.string.no_email));
            email_text.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (password.isEmpty()) {
            password_text.setError(getString(R.string.no_password));
            password_text.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        // Create user with Firebase Authentication using the provided details
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // Check the account was created successfully
                        if (task.isSuccessful()) {
                            Log.d(TAG, "created user");

                            // Change to the Set Up Account UI
                            Intent i = new Intent(SignInActivity.this, SetUpAccountActivity.class);
                            i.putExtra("email",email);
                            startActivity(i);
                        } else {

                            // Inform the user what was wrong with their input
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                Log.d(TAG, "User collision on sign up");
                                email_text.setError(getString(R.string.email_taken));
                                email_text.requestFocus();
                                progressBar.setVisibility(View.INVISIBLE);
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {

                                // We put a toast here since it is not clear whether the email or
                                // password caused the error, thus focused error messages could be
                                // misleading
                                Log.d(TAG,"User did not enter a correctly formed email or password");
                                Toast.makeText(getApplicationContext(), R.string.bad_email_password,
                                        Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            } else {
                                Log.w(TAG, "A uncaught exception was thrown",e);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });
    }

    /*
     *  setUpListeners
     *
     *  Connects button's to their respective on click functions
     *
     */
    private void setUpListeners() {
        Button signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onSignInPressed();
            }
        });
        Button signUpButton = (Button) findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpPressed();
            }
        });

    }

}
