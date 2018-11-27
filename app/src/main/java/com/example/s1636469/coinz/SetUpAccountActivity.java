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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import io.grpc.Context;

public class SetUpAccountActivity extends Activity {

    private FirebaseAuth mAuth;
    private Bitmap profile_img;
    private String TAG = "SetUpAccount";
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_account);
        mAuth = FirebaseAuth.getInstance();
        email = getIntent().getStringExtra("email");
        setUpListeners();
    }
    private void setUpListeners() {
        Button continue_to_app = (Button) findViewById(R.id.continue_to_app);
        continue_to_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = ((EditText) findViewById(R.id.display_name)).getText().toString();

                String u_id = mAuth.getCurrentUser().getUid();
                Log.d(TAG, u_id);

                String path = "user_images/" + u_id;
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReference();
                StorageReference profile_ref = storageReference.child(path);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                if (profile_img == null) {
                    profile_img = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.blank_profile);
                }

                profile_img.compress(Bitmap.CompressFormat.JPEG,80,baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = profile_ref.putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //continue;
                        FirebaseFirestore database =FirebaseFirestore.getInstance();
                        DocumentReference to_add = database.collection("users").document(u_id);
                        HashMap<String, Object> new_user_details = new HashMap<String, Object>(Config.blank_user_profile);
                        new_user_details.put("name",display_name);
                        new_user_details.put("profile_url",path);
                        new_user_details.put("email",email);
                        to_add.set(new_user_details).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("id",u_id);
                                editor.putString("name",display_name);
                                editor.commit();

                                // Start up the main app
                                Intent i = new Intent(SetUpAccountActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG,"Something went wrong during sign up",e);
                                Toast.makeText(getApplicationContext(), "Could not create user " +
                                        "at this time, please try again later",Toast.LENGTH_SHORT).show();
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
                    }
                });

                // set up database entry

                // load up app
            }
        });

        Button choose_image = (Button) findViewById(R.id.choose_image);
        choose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),Config.PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                profile_img = BitmapFactory.decodeStream(inputStream);
                CircularImageView civ = (CircularImageView) this.findViewById(R.id.profile_img_preview);
                civ.setImageBitmap(profile_img);
            } catch (FileNotFoundException e) {
                Log.w(TAG, "Cannot find file", e);
            }
        }
    }
}