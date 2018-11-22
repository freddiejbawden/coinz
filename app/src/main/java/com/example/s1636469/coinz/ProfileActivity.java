package com.example.s1636469.coinz;

import android.content.Context;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fillInValues();
        Intent i = getIntent();
        boolean is_friend = i.getBooleanExtra("already_friends",true);
        setUpListeners(is_friend);
    }

    protected void fillInValues() {
        if (FriendListFragment.pass_to_profile != null ){
            String name = (String) FriendListFragment.pass_to_profile.get("name");
            Bitmap img = (Bitmap) FriendListFragment.pass_to_profile.get("img");
            HashMap<String, Double> curs = (HashMap<String, Double>) FriendListFragment.pass_to_profile.get("currencies");

            //TODO: get higher res img
            CircularImageView profile_img = (CircularImageView) findViewById(R.id.profile_img);
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
        FloatingActionButton friend_button = (FloatingActionButton) findViewById(R.id.profile_friend_button);
        String f_uname = (String) FriendListFragment.pass_to_profile.get("name");
        updateButton(is_friend, f_uname);
    }

    private void addFriend(String friend_uname) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //TODO: Replace Placeholder name with localStorage retrival
        DocumentReference user_ref = database.collection("users").document("initial");
        Log.d(TAG, "friend name %s".format(friend_uname));
        DocumentReference friend_ref = database.collection("users").document(friend_uname);
        friend_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> data = (Map<String, Object>) documentSnapshot.getData();
                if (data != null) {
                    String profile_url = (String) data.get("profile_url");
                    String profile_name = (String) data.get("username");
                    HashMap<String, Object> array_element = new HashMap<>();
                    array_element.put("name",profile_name);
                    array_element.put("profile_url", profile_url);
                    user_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            ArrayList<Object> friends = (ArrayList<Object>) documentSnapshot.getData().get("friends");
                            friends.add(array_element);
                            HashMap<String, Object> to_put = new HashMap<>();
                            to_put.put("friends",friends);
                            user_ref.set(to_put, SetOptions.merge()).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG,"",e);
                                    Toast.makeText(getApplicationContext(), "Unable to add friend!", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Added as friend!", Toast.LENGTH_SHORT).show();
                                    updateButton(true,friend_uname);
                                }
                            });
                        }
                    });
                } else {
                    Log.w(TAG, "friend data is null!");
                }

            }
        });
    }

    public void removeFriend(String friend_uname) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //TODO: Replace Placeholder name with localStorage retrival
        DocumentReference user_ref = database.collection("users").document("initial");
        user_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> data = (Map<String, Object>) documentSnapshot.getData();
                ArrayList<Map<String, Object>> friends = (ArrayList<Map<String, Object>>) data.get("friends");
                int i = 0;
                boolean found = false;
                for (Map<String, Object> friend : friends) {
                    if (friend.get("name").equals(friend_uname)) {
                        friends.remove(i);
                        found = true;
                        break;
                    }
                    i++;
                }
                if (found) {
                    HashMap<String, Object> to_put = new HashMap<String, Object>();
                    to_put.put("friends",friends);
                    user_ref.set(to_put, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"Removed friend",Toast.LENGTH_SHORT).show();
                            updateButton(false,friend_uname);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"firebase error caused error in removing friend");
                            Toast.makeText(getApplicationContext(),"Unable to remove friend",Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.w(TAG, "Unable to find friend in list");
                    Toast.makeText(getApplicationContext(),"Unable to remove friend!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateButton(boolean is_friend, String friend_uname) {
        Log.d(TAG,"update button");
        FloatingActionButton friend_button = (FloatingActionButton) findViewById(R.id.profile_friend_button);
        if (is_friend) {
            friend_button.setImageResource(R.drawable.remove_white);
            friend_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"Remove Button clicked");
                    friend_button.setEnabled(false);
                    removeFriend(friend_uname);
                }
            });
        } else {
            friend_button.setImageResource(R.drawable.add_white);
            friend_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG,"Add Button clicked");
                    friend_button.setEnabled(false);
                    addFriend(friend_uname);
                }
            });
        }
        friend_button.setEnabled(true);
    }
}
