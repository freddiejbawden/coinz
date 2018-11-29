package com.example.s1636469.coinz;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;


public class FriendListFragment extends Fragment {
    public static Bundle pass_to_profile;
    private View rootView;
    private RecyclerView mRecyclerView;
    public FriendListAdapter mFriendAdapter;
    private ArrayList<FriendsInfo> data = new ArrayList<FriendsInfo>();
    private String TAG = "FriendsListFragment";


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

        setUpListeners();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();
        setUpListeners();
        getFriends(id);

        Log.d(TAG,"getting message");
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();

        getFriends(id);

    }

    private void setUpListeners() {
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                //Disable item
                View item = mRecyclerView.findChildViewUnder(motionEvent.getX(),motionEvent.getY());

                if (item == null) {
                    return false;
                }
                item.setEnabled(false);
                Log.d(TAG,"Loading Profile");

                int position = mRecyclerView.getChildLayoutPosition(item);
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build();
                database.setFirestoreSettings(settings);
                FriendsInfo toDisplay = data.get(position);

                DocumentReference documentReference = database.collection("users").document(toDisplay.getId());
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.getData().isEmpty()) {
                            Map<String, Object> details = documentSnapshot.getData();
                            HashMap<String, Double> curs = new HashMap<String,Double>();
                            for (String cur : Config.currencies) {
                                try {
                                    curs.put(cur, (Double) details.get(cur));
                                } catch (ClassCastException e) {
                                    curs.put(cur, ((Long) details.get(cur)).doubleValue());
                                }

                            }
                            try {
                                curs.put("GOLD",(Double) details.get("GOLD"));
                            } catch (ClassCastException e) {
                                curs.put("GOLD",((Long)  details.get("GOLD")).doubleValue());
                            }

                            Date last_log = (Date) details.get("last_login");

                            Bundle bundle = new Bundle();
                            bundle.putParcelable("img",toDisplay.getImg());
                            bundle.putSerializable("date",(Date) details.get("last_login"));
                            bundle.putString("name",toDisplay.getName());
                            bundle.putSerializable("currencies", curs);
                            bundle.putString("id",documentSnapshot.getId());
                            pass_to_profile = bundle;
                            System.out.println(getParentFragment().getActivity());
                            Intent i = new Intent(item.getContext(), ProfileActivity.class);
                            i.setFlags(FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            startActivity(i);
                        } else {
                            Toast.makeText(getContext(), "Cannot find user! Please try again later",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });
    }

    public void getImageRecursive(List<DocumentSnapshot> l_friends, ArrayList<FriendsInfo> toAdd, Context c) {
        if (l_friends.isEmpty()) {
            Log.d("STATUS","Empty");
            data.clear();
            Log.d(TAG, toAdd.toString());
            data.addAll(toAdd);
            mFriendAdapter.notifyDataSetChanged();
            Log.d(TAG,"Changing adapter");
            return;

        } else {
            HashMap<String, Object> friend_map = (HashMap<String, Object>) l_friends.get(0).getData();
            Log.d("STATUS",friend_map.toString());
            final String profile_name = (String) friend_map.get("name");
            final String profile_url = (String) friend_map.get("profile_url");

            //Change reference to id
            final String f_id = l_friends.get(0).getId();
            Log.d(TAG, (f_id == l_friends.get(0).getId()) + "");
            Log.d(TAG, "f_id: " + f_id);
            final FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            StorageReference pathReference = storageReference.child(profile_url);

            // Images compressed on sign up

            pathReference.getBytes(Config.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    //Data for image is retuned
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    Bitmap bitmap;
                    if (inputStream != null) {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } else {
                        Log.w("STATUS","inputStream is null");
                        bitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.blank_profile);
                    }

                    toAdd.add(new FriendsInfo(profile_name, bitmap, f_id));
                    l_friends.remove(0);
                    Log.d(TAG, "f_id after call " + f_id);
                    getImageRecursive(l_friends, toAdd,c);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Failed to get image");
                    int errorCode = ((StorageException) e).getErrorCode();

                    String profile_name = (String) friend_map.get("name");
                    Bitmap bitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.blank_profile);
                    toAdd.add(new FriendsInfo(profile_name, bitmap,f_id));
                    l_friends.remove(0);
                    getImageRecursive(l_friends, toAdd,c);
                }
            });

        }
    }

    public void getFriends(String username) {
        FirebaseFirestore.setLoggingEnabled(false);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        database.setFirestoreSettings(settings);

        ArrayList<FriendsInfo> toAdd = new ArrayList<>();
        Context c = this.getContext();

        Log.d(TAG,"getting " + username  + " friends");

        final CollectionReference friendsRef = database.collection("users").document(username).collection("friends");
        friendsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> u_friends_data = queryDocumentSnapshots.getDocuments();
                Log.d(TAG,"here");
                getImageRecursive(u_friends_data, new ArrayList<FriendsInfo>(),getContext());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //TODO: Check exceptions thrown
                Toast.makeText(getContext(), "Could not get friends!", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error thrown when getting friends", e);

            }
        });

    }

}
