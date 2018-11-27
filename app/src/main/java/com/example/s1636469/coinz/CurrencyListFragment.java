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

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CurrencyListFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private HashMap<String, Long> totals = new HashMap<String, Long>();
    private HashMap<String, Float> oldTrends = new HashMap<String, Float>();
    ArrayList<CurrencyInfo> data = new ArrayList<CurrencyInfo>();
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View rootView;

    public void updateData(Map<String, Object> docData) {
        HashMap<String, Float> values = new HashMap<String, Float>();
        long sum = 0l;
        for (String key : docData.keySet()) {
            sum += (long) docData.get(key);
        }
        data.clear();
        ArrayList<CurrencyInfo> toAdd = new ArrayList<CurrencyInfo>();
        for (String key : docData.keySet()) {
            float newVal = (float) ((long) docData.get(key))/sum;
            float trend = (newVal - oldTrends.get(key));
            oldTrends.put(key, trend);
            toAdd.add(new CurrencyInfo(key, newVal, trend));
        }
        data.addAll(toAdd);
        Log.d("CUR",data.toString());
        mAdapter.notifyDataSetChanged();
    }


    protected void setUpListeners() {
        FirebaseFirestore.setLoggingEnabled(false);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        database.setFirestoreSettings(settings);
        final DocumentReference docRef = database.collection("bank").document("totals");

        docRef.addSnapshotListener(new com.google.firebase.firestore.EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("STATUS", "Listen Failed. ", e);
                    return;
                }
                Map<String, Object> docData = documentSnapshot.getData();
                int i =0;
                for (String key : docData.keySet()) {
                    long val = (long) docData.get(key);
                    if (totals.get(key) != val) {
                        updateData(docData);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        totals.put("PENY", 0l);
        totals.put("QUID", 0l);
        totals.put("DOLR", 0l);
        totals.put("SHIL", 0l);
        oldTrends.put("PENY",0f);
        oldTrends.put("QUID",0f);
        oldTrends.put("DOLR",0f);
        oldTrends.put("SHIL",0f);


        setUpListeners();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.wallet_contents_recycler, container, false);
        // 1. get a reference to recyclerView
        mRecyclerView= (RecyclerView) rootView.findViewById(R.id.currency_recycler);

        // 2. set layoutManger
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new CurrencyListAdapter(getContext(),data);
        // 4. set adapter
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }
}
