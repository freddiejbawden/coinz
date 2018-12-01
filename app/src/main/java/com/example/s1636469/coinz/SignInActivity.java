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
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.login_progress);
        progressBar.setVisibility(View.INVISIBLE);
        email_text = findViewById(R.id.email_edit_text);
        password_text = findViewById(R.id.password_edit_text);
        setUpListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void check_user(String id,String email) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference u_ref = database.collection("users").document(id);
        u_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> u_data = documentSnapshot.getData();

                if (!documentSnapshot.exists()) {
                    Intent i = new Intent(SignInActivity.this, SetUpAccountActivity.class);
                    i.putExtra("email",email);
                    startActivity(i);
                    return;
                }

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


    private void setUpListeners() {
        Button signInButton = (Button) findViewById(R.id.signin_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // we have been logged in!
                            //TODO: disable buttons

                            Log.d(TAG, "signed in");
                            check_user(mAuth.getCurrentUser().getUid(),email);
                            // pass to another intent
                        } else {
                            Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Log.d(TAG,"User did not enter a correctly formed email or password");
                            Toast.makeText(getApplicationContext(), R.string.bad_email_password,
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            Log.w(TAG, "A uncaught exception was thrown",e);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
        Button signUpButton = (Button) findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "created user");
                                    Intent i = new Intent(SignInActivity.this, SetUpAccountActivity.class);
                                    i.putExtra("email",email);
                                    startActivity(i);
                                } else {
                                    Exception e = task.getException();
                                    if (e instanceof FirebaseAuthUserCollisionException) {
                                        Log.d(TAG, "User collision on sign up");
                                        email_text.setError(getString(R.string.email_taken));
                                        email_text.requestFocus();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
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
        });

    }

}
