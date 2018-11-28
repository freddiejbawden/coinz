package com.example.s1636469.coinz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;

public class SearchFriendsFragment extends Fragment {

    private View rootView;
    private RecyclerView mRecyclerView;
    public FriendListAdapter mFriendAdapter;
    private ArrayList<FriendsInfo> data = new ArrayList<FriendsInfo>();
    private String TAG = "FriendsSearchFragment";
    @Override
    @NonNull
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_friends, container, false);
        // 1. get a reference to recyclerView
        mRecyclerView= (RecyclerView) rootView.findViewById(R.id.search_friends_list);

        // 2. set layoutManger
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mFriendAdapter = new FriendListAdapter(getContext(),data);
        // 4. set adapter
        mRecyclerView.setAdapter(mFriendAdapter);
        setUpListeners();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        data.clear();
    }

    public void getUserRecursive(List<DocumentSnapshot> l_friends, ArrayList<FriendsInfo> toAdd) {
        ArrayList<DocumentSnapshot> friends = (ArrayList<DocumentSnapshot>) l_friends;

        if (friends.isEmpty()) {

            Log.d("STATUS","Empty");
            data.clear();
            data.addAll(toAdd);
            mFriendAdapter.notifyDataSetChanged();
            Log.d(TAG,"Changing adapter");
            return;

        } else {

            Map<String, Object> friend_map = (Map<String, Object>) friends.get(0).getData();
            Log.d("STATUS",friend_map.toString());
            String profile_name = (String) friend_map.get("name");
            String profile_url = (String) friend_map.get("profile_url");
            String id = friends.get(0).getId();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();


            StorageReference pathReference = storageReference.child(profile_url);

            // Images compressed on sign up
            final long ONE_MEGABYTE = 1024 * 1024;
            pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    //Data for image is retuned
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                    Bitmap bitmap;
                    if (inputStream != null) {
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } else {
                        Log.w("STATUS","inputStream is null");
                        bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.blank_profile);
                    }
                    toAdd.add(new FriendsInfo(profile_name, bitmap,id));
                    friends.remove(0);
                    getUserRecursive(friends, toAdd);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "Failed to get image");
                    int errorCode = ((StorageException) e).getErrorCode();

                    String profile_name = (String) friend_map.get("name");
                    Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.blank_profile);
                    toAdd.add(new FriendsInfo(profile_name, bitmap,id));
                    friends.remove(0);
                    getUserRecursive(friends, toAdd);
                }
            });

        }
    }

    private void queryEmail(CollectionReference users, String query) {
        Query q_email = users.whereEqualTo("email",query);
        q_email.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> friends = queryDocumentSnapshots.getDocuments();
                if (friends.isEmpty()) {
                    Toast.makeText(getContext(), "Could not find user " + query,
                            Toast.LENGTH_SHORT).show();
                } else {
                    getUserRecursive(friends,new ArrayList<FriendsInfo>());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "fail");
            }
        });
    }

    private void searchQuery() {
        SearchView searchView = getActivity().findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Log.d("STATUS","Search");

                FirebaseFirestore database = FirebaseFirestore.getInstance();
                CollectionReference users = database.collection("users");
                Query q_name = users.whereEqualTo("name",query);
                q_name.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> friends = queryDocumentSnapshots.getDocuments();
                        if (friends.isEmpty()) {
                            queryEmail(users,query);
                        } else {
                            getUserRecursive(friends, new ArrayList<FriendsInfo>());
                        }
                    }
                });
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("STATUS","update");
                return false;
            }
        });
    }

    private void setUpListeners() {
        searchQuery();
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                View item = mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if (item == null) {
                    return false;
                }
                item.setEnabled(false);

                Log.d(TAG, "loading profile...");

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
                        if (documentSnapshot.getData() != null) {
                            Map<String, Object> details = documentSnapshot.getData();
                            HashMap<String, Double> curs = new HashMap<String, Double>();
                            for (String cur : Config.currencies) {
                                try {
                                    curs.put(cur, (Double) details.get(cur));
                                } catch (ClassCastException e) {
                                    curs.put(cur, ((Long) details.get(cur)).doubleValue());
                                }

                            }
                            try {
                                curs.put("GOLD", (Double) details.get("GOLD"));
                            } catch (ClassCastException e) {
                                curs.put("GOLD", ((Long) details.get("GOLD")).doubleValue());
                            }
                            Date last_log = (Date) details.get("last_login");

                            Bundle bundle = new Bundle();
                            bundle.putParcelable("img", toDisplay.getImg());
                            bundle.putSerializable("date", (Date) details.get("last_login"));
                            bundle.putString("name", toDisplay.getName());
                            bundle.putSerializable("currencies", curs);
                            bundle.putString("id",documentSnapshot.getId());
                            FriendListFragment.pass_to_profile = bundle;
                            System.out.println(getParentFragment().getActivity());
                            Intent i = new Intent(item.getContext(), ProfileActivity.class);
                            i.setFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            startActivity(i);
                        } else {
                            Toast.makeText(getContext(), "Cannot find user! Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                return;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {
                return;
            }
        });
    }
}
