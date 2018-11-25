package com.example.s1636469.coinz;

import android.media.session.MediaController;
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
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment {

    private View rootView;
    private String current_view;
    private Spinner leaderboardSpinner;
    private RecyclerView mRecyclerView;
    private LeaderboardAdapter mAdapter;
    private ArrayList<PlayerInfo> data = new ArrayList<>();
    private String TAG = "Leaderboard";

    @Override
    @NonNull
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_leaderboard,container,false);
        leaderboardSpinner = (Spinner) rootView.findViewById(R.id.leaderboard_spinner);
        mRecyclerView = rootView.findViewById(R.id.leaderboard_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new LeaderboardAdapter(getContext(), data);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpListeners();
        populateSpinner();
        setLeaderboard();
    }

    private void setUpListeners() {
        leaderboardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] leaderboard_arr = getResources().getStringArray(R.array.leaderboards);
                if (!leaderboard_arr[position].equals(current_view)) {
                    setLeaderboard();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }


    private void setLeaderboard() {
        String leaderboard_type = (String) leaderboardSpinner.getSelectedItem();
        if (leaderboard_type.equals("All Time")) {
            populateAllUserLeaderboard("GOLD");
        } else if (leaderboard_type.equals("Weekly")) {
            populateAllUserLeaderboard("weekly_GOLD");
        }
        current_view = leaderboard_type;
    }
    private void populateAllUserLeaderboard(String order_field) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference users_ref = database.collection("users");
        users_ref.orderBy("GOLD", Query.Direction.DESCENDING).limit(10).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> to_display = queryDocumentSnapshots.getDocuments();
                ArrayList<PlayerInfo> playerInfos = new ArrayList<>();
                int i = 1;
                for (DocumentSnapshot documentSnapshot : to_display) {
                    Map<String, Object> user_data = documentSnapshot.getData();
                    String name = (String) user_data.get("username");
                    String gold;
                    try {
                        Double gold_val = (Double) user_data.get(order_field);
                        gold = gold_val.toString();
                    } catch (ClassCastException e) {
                        Long gold_val = ((Long) user_data.get(order_field));
                        gold = gold_val.toString();
                    }
                    if (Double.parseDouble(gold) > 0) {
                        PlayerInfo pi = new PlayerInfo(i+"",name,gold);
                        Log.d(TAG,pi.toString());
                        playerInfos.add(pi);
                        i++;
                    }

                }
                Log.d(TAG,playerInfos.size() + "");
                data.clear();
                data.addAll(playerInfos);
                Log.d(TAG,data.toString());
                mAdapter.notifyDataSetChanged();
            }
        });
    }


    private void populateSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.leaderboards,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaderboardSpinner.setAdapter(adapter);
    }

}
