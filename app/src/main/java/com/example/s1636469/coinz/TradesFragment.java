package com.example.s1636469.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;

public class TradesFragment extends Fragment {

    private View rootView;
    private RecyclerView mRecyclerView;
    public TradeAdapter mTradeAdapter;
    private ArrayList<TradeData> data = new ArrayList<TradeData>();
    private String TAG = "TradesFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_trades,container,false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_trade_history);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTradeAdapter = new TradeAdapter(getContext(), data);
        mRecyclerView.setAdapter(mTradeAdapter);
        return rootView;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpListeners();
        getTrades();
    }
    private void setUpListeners() {
        FloatingActionButton send_coins = getActivity().findViewById(R.id.send_trade_button);
        send_coins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), SendTradeActivity.class);
                i.setFlags(FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                i.putExtra("already_friends",true);
                startActivity(i);
            }
        });
    }
    private void getTrades() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        //TODO: Pull user from server
        DocumentReference dRef = database.collection("users").document("initial");
        //TODO: Get last ten and spin o/w
        dRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "send it");
                //TODO: check if null
                Map<String, Object> f_store_data = documentSnapshot.getData();
                List<Object> trades = (List<Object>) f_store_data.get("trades");
                ArrayList<TradeData> toAdd = new ArrayList<>();
                Log.d(TAG,trades.size() + "");
                for (Object t : trades) {
                    Map<String, Object> trade_data = (Map<String, Object>) t;
                    boolean sent_by_user = (boolean) trade_data.get("sent_by_user");
                    String other_user = (String) trade_data.get("other_user");
                    String amount = (String) trade_data.get("amount");
                    String cur =   (String) trade_data.get("currency");
                    //TODO: change to logged in user
                    if (sent_by_user) {
                        toAdd.add(new TradeData(sent_by_user, "initial", other_user, amount,cur));
                    } else {
                        toAdd.add(new TradeData(sent_by_user, other_user, "initial", amount,cur));
                    }
                    Log.d(TAG, "added");
                }
                data.clear();
                data.addAll(toAdd);
                mTradeAdapter.notifyDataSetChanged();
            }
        });
    }
}
