/*
 *  LeaderboardFragment
 *
 *  Handles displaying the leaderboard information
 *
 */
package com.example.s1636469.coinz;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment {

    private View rootView;
    private RecyclerView mRecyclerView;
    private LeaderboardAdapter mAdapter;
    private ArrayList<PlayerInfo> data = new ArrayList<>();
    private Spinner leaderboardSpinner;
    private String TAG = "Leaderboard";
    private ProgressBar progressBar;

    @Override
    @NonNull
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_leaderboard,container,false);

        // Get reference to UI elements
        mRecyclerView = rootView.findViewById(R.id.leaderboard_recycler);
        progressBar = rootView.findViewById(R.id.leaderboard_progress);
        leaderboardSpinner = rootView.findViewById(R.id.leaderboardSpinner);

        // Set up recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new LeaderboardAdapter(getContext(), data);
        mRecyclerView.setAdapter(mAdapter);

        progressBar.setVisibility(View.INVISIBLE);

        setUpListeners();
        populateSpinner();
        populateAllUserLeaderboard();

        return rootView;
    }

    /*
     *  populateSpinner
     *
     *  Populate leaderboard spinner with options from menu resource
     */
    private void populateSpinner() {
        Context c = getContext();
        if (c != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.leaderboards,
                    android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            leaderboardSpinner.setAdapter(adapter);
        } else {
            Log.d(TAG, "Context was null in populateSpinner!");
        }
    }
    /*
     *  setUpListeners
     *
     *  sets up listener for leaderboard spinner
     *
     */
    private void setUpListeners() {
        leaderboardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] leaderboard_arr = getResources().getStringArray(R.array.leaderboards);
                if (leaderboard_arr[position].equals("Friends")) {
                    populateFriendsLeaderboard();
                } else {
                    populateAllUserLeaderboard();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                populateAllUserLeaderboard();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateAllUserLeaderboard();
    }

    /*
     *  populateFriendsLeaderboard
     *
     *  gets the friends from the user's collection and display them
     *
     */
    private void populateFriendsLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);

        // Get reference from database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "Firebase User was null");
            Toast.makeText(getContext(), "Cannot get friends, please try again later",Toast.LENGTH_SHORT).show();
            return;
        }

        String id = auth.getCurrentUser().getUid();
        CollectionReference u_friends= database.collection("users").document(id).collection("friends");

        // Get all friends and sort by GOLD value
        u_friends.orderBy("GOLD", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List<DocumentSnapshot> to_display = queryDocumentSnapshots.getDocuments();

                DocumentReference u_data = database.collection("users").document(id);
                u_data.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        drawFriendsLeaderboard(documentSnapshot, to_display);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Could not display friends leaderboard",
                        Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    /*
     *  drawFriendsLeaderboard
     *
     *  Updates the friends leaderboard
     *
     */
    private void drawFriendsLeaderboard(DocumentSnapshot documentSnapshot, List<DocumentSnapshot> to_display) {

        Map<String, Object> u_data_map = documentSnapshot.getData();
        if (u_data_map == null) {
            Log.d(TAG, "User data is null");
            Toast.makeText(getContext(), "Cannot get all time leaderboard", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean user_score_added = false;
        double user_gold;
        try {
            user_gold = (Double) u_data_map.get("GOLD");
        } catch (ClassCastException e) {
            user_gold = ((Long) u_data_map.get("GOLD")).doubleValue();
        }

        ArrayList<PlayerInfo> playerInfos = new ArrayList<>();

        int counter = 1;

        // Display each friend's data
        for (DocumentSnapshot d : to_display) {

            Map<String, Object> user_data = d.getData();
            if (user_data == null) {
                Log.d(TAG, "User data is null");
                Toast.makeText(getContext(), "Cannot get friend leaderboard", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = (String) user_data.get("name");
            double gold;
            try {
                gold = (Double) user_data.get("GOLD");
            } catch (ClassCastException e) {
                gold = ((Long) user_data.get("GOLD")).doubleValue();
            }

            if (gold > 0) {
                // Check if the user has more gold
                if (user_gold > gold && !user_score_added) {

                    // Add user to list
                    playerInfos.add(new PlayerInfo(counter+"","You",user_gold+""));
                    counter++;
                    user_score_added = true;
                }

                // Add friend to list
                PlayerInfo pi = new PlayerInfo(counter+"",name,gold + "");
                playerInfos.add(pi);
                counter++;
            }
        }
        // Check friend list
        if (!user_score_added) {
            playerInfos.add(new PlayerInfo(counter+"","You",user_gold+""));

        }

        // Update Recycler View
        Log.d(TAG,playerInfos.size() + "");
        data.clear();
        data.addAll(playerInfos);
        Log.d(TAG,data.toString());
        progressBar.setVisibility(View.INVISIBLE);
        mAdapter.notifyDataSetChanged();
    }
    /*
     *  populateAllUserLeaderboard
     *
     *  draws the all time leaderboard
     */
    private void populateAllUserLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);

        //Get database reference
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference users_ref = database.collection("users");

        // Get top 10 users and order by gold value
        users_ref.orderBy("GOLD", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                drawAllTimeLeaderboard(queryDocumentSnapshots);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Could not get all time leaderboard",
                        Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    /*
     *  drawAllTimeLeaderboard
     *
     *  updates the all time leaderboard
     */
    private void drawAllTimeLeaderboard(QuerySnapshot queryDocumentSnapshots) {
        List<DocumentSnapshot> to_display = queryDocumentSnapshots.getDocuments();
        ArrayList<PlayerInfo> playerInfos = new ArrayList<>();

        int counter = 1;
        // Add each user to the list
        for (DocumentSnapshot d : to_display) {
            Map<String, Object> user_data = d.getData();
            if (user_data == null) {
                Log.d(TAG, "User data is null");
                Toast.makeText(getContext(), "Cannot get all time leaderboard", Toast.LENGTH_SHORT).show();
                return;
            }
            String name = (String) user_data.get("name");
            double gold;
            try {
                gold = (Double) user_data.get("GOLD");
            } catch (ClassCastException e) {
                gold = ((Long) user_data.get("GOLD")).doubleValue();
            }

            // If player has more than 0 gold add them to the list
            if (gold > 0) {
                PlayerInfo pi = new PlayerInfo(counter+"",name,gold + "");
                playerInfos.add(pi);
                counter++;
            }
        }

        // Update recycler view
        Log.d(TAG,playerInfos.size() + "");
        data.clear();
        data.addAll(playerInfos);
        Log.d(TAG,data.toString());
        progressBar.setVisibility(View.INVISIBLE);
        mAdapter.notifyDataSetChanged();
    }
}
