package com.example.s1636469.coinz;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class FriendListFragment extends Fragment {
    private View rootView;
    private RecyclerView mRecyclerView;
    public FriendListAdapter mFriendAdapter;
    private ArrayList<FriendsInfo> data = new ArrayList<FriendsInfo>();
    private String TAG = "FriendsListFragment";

    public void getImageRecursive(Object o_friends, ArrayList<FriendsInfo> toAdd) {
        ArrayList<Object> friends = (ArrayList<Object>) o_friends;
        if (friends.isEmpty()) {
            Log.d("STATUS","Empty");
            data.clear();
            data.addAll(toAdd);
            mFriendAdapter.notifyDataSetChanged();
            Log.d(TAG,"Changing adapter");
            return;
        } else {
            Log.d("STATUS","It");
            HashMap<String, Object> friend_map = (HashMap<String, Object>) friends.get(0);
            Log.d("STATUS",friend_map.toString());
            String profile_url = (String) friend_map.get("profile_url");
            String profile_name = (String) friend_map.get("name");
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            //TODO: feed url to async below
            StorageReference pathReference = storageReference.child(profile_url);
            //TODO: SUPER COMPRESS THE IMAGES!
            final long ONE_MEGABYTE = 1024 * 1024;
            pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    //Data for image is retuned
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    if (inputStream != null) {
                        Log.d("STATUS","image is not null");
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        toAdd.add(new FriendsInfo(profile_name, bitmap));
                        friends.remove(0);
                        getImageRecursive(friends, toAdd);
                    }
                    Log.w("STATUS","Bitmap is null");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //return default image
                }
            });
        }
    }

    public void getMessagePreviews(String username) {
        FirebaseFirestore.setLoggingEnabled(false);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        database.setFirestoreSettings(settings);

        ArrayList<FriendsInfo> toAdd = new ArrayList<>();

        final DocumentReference docRef = database.collection("users").document(username);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    HashMap<String, Object> profile = (HashMap<String, Object>) task.getResult().getData();
                    getImageRecursive(profile.get("friends"), new ArrayList<FriendsInfo>());
                }
            }
        });

    }
    @Override
    @NonNull
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_friend_list, container, false);
        // 1. get a reference to recyclerView
        mRecyclerView= (RecyclerView) rootView.findViewById(R.id.friends_list_recycler);

        // 2. set layoutManger
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mFriendAdapter = new FriendListAdapter(getContext(),data);
        // 4. set adapter
        mRecyclerView.setAdapter(mFriendAdapter);
        // TODO: Get from uname storage
        getMessagePreviews("initial");
        Log.d(TAG,"getting message");
        return rootView;
    }
}
