/*
 *  ProfileActivity
 *
 *  Displays player information and allows users to add and remove friends
 *
 */

package com.example.s1636469.coinz;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProfileActivity extends Activity {
    protected String TAG = "Profile";
    private String friend_id;
    private FloatingActionButton friend_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        friend_button =  findViewById(R.id.profile_friend_button);
        friend_button.setEnabled(true);
        fillInValues();

        friend_id = FriendListFragment.pass_to_profile.getString("id");
        checkIfFriends();

    }
    /*
     *  checkIfFriends
     *
     *  check the users's friends collection, if they aer friends then return true else false
     */
    private void checkIfFriends() {

        String user_id = FirebaseAuth.getInstance().getUid();
        if (user_id == null) {
            friend_button.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Cannot get friend data right now, so you " +
                    "cannot manage thier friend status", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference f_ref = database.collection("users")
                .document(user_id)
                .collection("friends")
                .document(friend_id);

        // Get friend collection
        f_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // if the friend collection has the user
                    if (task.getResult() != null && task.getResult().exists()) {
                        setUpListeners(true);
                    } else {
                        setUpListeners(false);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Could not find friend!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /*
     *  fillInValues
     *
     *  update UI with data from Bundle
     */
    protected void fillInValues() {
        if (FriendListFragment.pass_to_profile != null ){

            // Get all data
            String name = (String) FriendListFragment.pass_to_profile.get("name");
            Bitmap img = (Bitmap) FriendListFragment.pass_to_profile.get("img");
            HashMap<String, Double> curs;
            try {
                curs = (HashMap<String, Double>) FriendListFragment.pass_to_profile.get("currencies");
            } catch (ClassCastException e) {
                Log.w(TAG, "Error thrown at filling in values",e);
                return;
            }

            // Display Data
            CircularImageView profile_img =  findViewById(R.id.profile_img_preview);
            profile_img.setImageBitmap(img);

            TextView name_view =  findViewById(R.id.profile_name);
            name_view.setText(name);

            // fill in all currencies
            LinearLayout ll =  findViewById(R.id.currency_amount_filler);
            if (curs == null) {
                Log.d(TAG, "Currency is null!");
                Toast.makeText(getApplicationContext(), "Cannot find currency values for player",Toast.LENGTH_SHORT).show();
                return;
            }

            for (String c : curs.keySet()) {
                TextView tv = new TextView(this);
                double rounded_cur = Config.round(curs.get(c), Config.CUR_VALUE_DP);
                tv.setText(c + ": " + rounded_cur);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setTextSize(20f);
                ll.addView(tv);
            }
        }
    }
    /*
     *  setUpListeners
     *
     *  sets up listeners for all of the buttons in the activity
     */
    private void setUpListeners(boolean is_friend) {
        ImageView close_icon = findViewById(R.id.close_profile);
        close_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        updateButton(is_friend);
    }

    /*
     *  addFriend
     *
     *  Add the player shown in profile as a friend
     *
     */
    private void addFriend() {
        // Get reference to the database
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User was null");
            Toast.makeText(getApplicationContext(), "Cannot add as friend",Toast.LENGTH_SHORT).show();
            return;
        }
        String id = auth.getCurrentUser().getUid();

        DocumentReference user_ref = database.collection("users").document(id);
        // Get user
        DocumentReference friend_ref = database.collection("users").document(friend_id);
        friend_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                addFriendFirebaseUpdate(documentSnapshot, user_ref);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error thrown on friend add", e);
                Toast.makeText(getApplicationContext(), "Cannot add friend!",Toast.LENGTH_SHORT).show();
            }
        });
    }
    /*
     *  addFriendFirebaseUpdate
     *
     *  Update FireStore with new friend
     */
    private void addFriendFirebaseUpdate(DocumentSnapshot documentSnapshot, DocumentReference user_ref) {
        Map<String, Object> data = documentSnapshot.getData();
        if (data != null) {

            // Get the nessesary data about the player
            String profile_url = (String) data.get("profile_url");
            String profile_name = (String) data.get("name");
            String id = documentSnapshot.getId();

            // Get data to push
            HashMap<String, Object> new_friend = new HashMap<>();
            new_friend.put("name",profile_name);
            new_friend.put("profile_url", profile_url);

            try {
                new_friend.put("GOLD",(Double) data.get("GOLD"));
            } catch (ClassCastException e) {
                new_friend.put("GOLD",((Long) data.get("GOLD")).doubleValue());
            }

            // Push the data and notify user
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

    /*
     *  removeFriend
     *
     *  removes friend from user's list
     */
    public void removeFriend() {

        // Get reference to firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();


        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "User was null");
            Toast.makeText(getApplicationContext(), "Cannot remove friend",Toast.LENGTH_SHORT).show();
            return;
        }
        String id = auth.getCurrentUser().getUid();

        // Get reference to remvoe
        DocumentReference remove_ref = database.collection("users").document(id)
                .collection("friends").document(friend_id);

        // Delete the document from the user's friends collection
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
    /*
     *  updateButton
     *
     *  change the add/remove button's functionality
     *
     */
    private void updateButton(boolean is_friend) {
        if (is_friend) {
            // Change image on button and onclick listener
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
            // Change image on button and onclick listener

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
        // enable button
        friend_button.setEnabled(true);
    }
}
