/*
 * SetUpAccountActivity
 *
 * Shown to the user when they are creating their account, allows the to provide a username and
 * profile image for their account
 *
 */

package com.example.s1636469.coinz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;


public class SetUpAccountActivity extends Activity {

    private FirebaseAuth mAuth;
    private Bitmap profile_img;
    private String TAG = "SetUpAccount";
    private String email;
    private ProgressBar progressBar;
    private EditText displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_account);

        // Get a reference to the Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Get references to UI elements
        email = getIntent().getStringExtra("email");
        progressBar = findViewById(R.id.set_up_progress);
        displayName = findViewById(R.id.display_name);

        progressBar.setVisibility(View.INVISIBLE);
        setUpListeners();
    }


    /*
     * convertProfileImgToBytes
     *
     * converts an uploaded profile image bitmap to a compressed byte array
     *
     */
    private byte[] convertProfileImgToBytes() {

        // user to store the image as bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // if the user has not chosen a profile image set a default one
        if (profile_img == null) {
            profile_img = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.blank_profile);
        }

        // Compress and output the image to a byte array
        profile_img.compress(Bitmap.CompressFormat.JPEG,80,baos);
        return baos.toByteArray();
    }


    /*
     * createAccount
     *
     *  sets up a user's account on Firebase using the template found in the Config Class
     *
     */
    private void createAccount(StorageReference profile_ref, byte[] image_bytes,
                               FirebaseFirestore database, String display_name,
                               String u_id, String image_path) {

        //Upload the image to Firebase Storage
        UploadTask uploadTask = profile_ref.putBytes(image_bytes);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //continue;

                // Get a reference to the user's document, it does not exist so it will be created
                // on setting
                DocumentReference to_add = database.collection("users").document(u_id);
                // Set up the document for the user
                HashMap<String, Object> new_user_details = new HashMap<String, Object>(Config.blank_user_profile);
                new_user_details.put("name", display_name);
                new_user_details.put("profile_url", image_path);
                new_user_details.put("email", email);
                to_add.set(new_user_details).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //TODO: maybe redundent?
                        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("id", u_id);
                        editor.putString("name", display_name);
                        editor.apply();

                        // Start up the tutorial
                        Intent i = new Intent(SetUpAccountActivity.this, TutorialActivity.class);
                        i.setFlags(i.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(i);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Something went wrong during sign up", e);
                        Toast.makeText(getApplicationContext(), "Could not create user " +
                                "at this time, please try again later", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Cancel continue
                Toast.makeText(getApplicationContext(), "We could not set up your " +
                                "account right now, please try again later",
                        Toast.LENGTH_SHORT).show();
                Log.w(TAG,"Failed to upload file",e);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    /*
     * onContinuePressed
     *
     * when the continue button is pressed, verify that the details entered are valid, then upload
     * to Firestore
     *
     */

    private void onContinuePressed() {
        progressBar.setVisibility(View.VISIBLE);
        String display_name = displayName.getText().toString();

        if (display_name.isEmpty()) {
            progressBar.setVisibility(View.INVISIBLE);
            displayName.setError(getString(R.string.no_username));
            displayName.requestFocus();
            return;
        }

        // Usernames can only be lowercase alphanumeric
        String username_parser ="([a-z]|[1-9])*";
        if (!display_name.matches(username_parser)) {
            progressBar.setVisibility(View.INVISIBLE);
            displayName.setError(getString(R.string.username_invalid));
            displayName.requestFocus();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "getCurrentUser returned null");
            Toast.makeText(getApplicationContext(), "An error occured when signing up" +
                    " your account, please try again later",Toast.LENGTH_SHORT).show();
            return;
        }

        String u_id = mAuth.getCurrentUser().getUid();

        Log.d(TAG, "Signing in: "+ u_id);

        // Create a path to where the users profile image will be stored
        String path = "user_images/" + u_id;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference profile_ref = storageReference.child(path);

        byte[] image_bytes = convertProfileImgToBytes();

        // Create a query to check if the chosen username is taken
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        Query q = database.collection("users").whereEqualTo("name",display_name);

        q.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                // If there are no documents that match the query the username is fine to use
                if(queryDocumentSnapshots.getDocuments().isEmpty()) {
                    createAccount(profile_ref,image_bytes,database,display_name,u_id,path);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    displayName.setError(getString(R.string.username_taken));
                    displayName.requestFocus();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "We could not set up your " +
                                "account right now, please try again later",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    /*
     * setUpListeners
     *
     * Set up functions to fire when user interacts with buttons
     *
     */
    private void setUpListeners() {
        Button continue_to_app = findViewById(R.id.continue_to_app);
        continue_to_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onContinuePressed();
            }
        });

        Button choose_image = findViewById(R.id.choose_image);
        choose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Use the default image selection activity to get the image
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                // Calls back to onActivityResult
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),Config.PICK_IMAGE);
            }
        });
    }


    /*
     * onActivityResult
     *
     * Process the image provided by the image picker
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                if (data.getData() == null) {
                    Log.d(TAG, "Image data is null");
                    Toast.makeText(getApplicationContext(), "An erro occured when trying to " +
                            "add your profile image, please try again later", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Conver the input stream to a bitmap
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                profile_img = BitmapFactory.decodeStream(inputStream);

                // Set the preview image to the bitmap
                CircularImageView civ =  this.findViewById(R.id.profile_img_preview);
                civ.setImageBitmap(profile_img);

            } catch (FileNotFoundException e) {
                Log.w(TAG, "Cannot find file", e);
            }
        }
    }
}
