package com.example.s1636469.coinz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends Activity {
    protected String TAG = "Profile";
    private String friend_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fillInValues();

        Intent i = getIntent();
        boolean is_friend = i.getBooleanExtra("already_friends",false);
        friend_id = FriendListFragment.pass_to_profile.getString("id");

        String user_id = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference f_ref = database.collection("users")
                .document("user_id")
                .collection("friends")
                .document(friend_id);
        f_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    setUpListeners(true);
                } else {
                    setUpListeners(false);
                }
            }
        });
    }

    protected void fillInValues() {
        if (FriendListFragment.pass_to_profile != null ){
            String name = (String) FriendListFragment.pass_to_profile.get("name");
            Bitmap img = (Bitmap) FriendListFragment.pass_to_profile.get("img");
            HashMap<String, Double> curs;
            try {
                curs = (HashMap<String, Double>) FriendListFragment.pass_to_profile.get("currencies");
            } catch (ClassCastException e) {
                Log.w(TAG, "Error thrown at filling in values",e);
                return;
            }


            CircularImageView profile_img = (CircularImageView) findViewById(R.id.profile_img_preview);
            profile_img.setImageBitmap(img);

            TextView name_view = (TextView) findViewById(R.id.profile_name);
            name_view.setText(name);

            LinearLayout ll = (LinearLayout) findViewById(R.id.currency_amount_filler);
            for (String k : curs.keySet()) {
                TextView tv = new TextView(this);
                Log.d(TAG,((Double)curs.get(k)).toString());
                tv.setText(k + ": " + curs.get(k).toString());
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setTextSize(20f);
                ll.addView(tv);
            }
        }
    }

    private void setUpListeners(boolean is_friend) {
        ImageView close_icon = (ImageView) findViewById(R.id.close_profile);
        close_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        updateButton(is_friend);
    }

    private void addFriend() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();
        DocumentReference user_ref = database.collection("users").document(id);
        Log.d(TAG, "friend name %s".format(friend_id));
        DocumentReference friend_ref = database.collection("users").document(friend_id);
        friend_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> data = (Map<String, Object>) documentSnapshot.getData();
                if (data != null) {
                    String profile_url = (String) data.get("profile_url");
                    String profile_name = (String) data.get("name");
                    String id = documentSnapshot.getId();

                    HashMap<String, Object> new_friend = new HashMap<>();
                    new_friend.put("name",profile_name);
                    new_friend.put("profile_url", profile_url);

                    user_ref.collection("friends").document(id).set(new_friend)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            updateButton(true);
                            Toast.makeText(getApplicationContext(), "Added " + profile_name +
                                "as friend",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Cannot add friend",
                                    Toast.LENGTH_SHORT).show();
                            Log.w(TAG,"Error thrown when adding friend",e);
                        }
                    });
                } else {
                    Log.w(TAG, "friend data is null!");
                    Toast.makeText(getApplicationContext(),"Cannot add friend!",Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error thrown on friend add", e);
                Toast.makeText(getApplicationContext(), "Cannot add friend!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeFriend() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();
        DocumentReference remove_ref = database.collection("users").document(id)
                .collection("friends").document(friend_id);
        remove_ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Removed from friends list!",
                        Toast.LENGTH_SHORT).show();
                updateButton(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Cannot remove friend!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButton(boolean is_friend) {
        Log.d(TAG,"update button");
        Log.d(TAG, "userid " + friend_id);
        FloatingActionButton friend_button = (FloatingActionButton) findViewById(R.id.profile_friend_button);
        if (is_friend) {
            friend_button.setImageResource(R.drawable.remove_white);
            friend_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"Remove Button clicked");
                    friend_button.setEnabled(false);
                    removeFriend();
                }
            });
        } else {
            friend_button.setImageResource(R.drawable.add_white);
            friend_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"Add Button clicked");
                    friend_button.setEnabled(false);
                    addFriend();
                }
            });
        }
        friend_button.setEnabled(true);
    }
}
