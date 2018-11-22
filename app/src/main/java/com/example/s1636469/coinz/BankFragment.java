package com.example.s1636469.coinz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Map;

public class BankFragment extends Fragment {


    private String TAG = "BankFragment";

    private RecyclerView mRecyclerView;
    private BankValuesAdapter mAdapter;
    private ArrayList<BankInfo> data = new ArrayList<>();
    private View rootView;

    @Override
    @NonNull
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.bank_fragment,container,false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.bank_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new BankValuesAdapter(getContext(), data);
        mRecyclerView.setAdapter(mAdapter);
        getBankDetails();
        setUpListeners();
        return rootView;
    }
    private void setUpListeners() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference dRef = database.collection("bank").document("totals");
        dRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                updateBankWithDocumentSnapshot(documentSnapshot);
            }
        });
    }
    private void updateBankWithDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        SharedPreferences sharedPref= getActivity().getSharedPreferences("bank", 0);
        SharedPreferences.Editor editor= sharedPref.edit();

        ArrayList<BankInfo> toAdd = new ArrayList<>();

        long total_coins = 0;
        Map<String, Object> bank_data = documentSnapshot.getData();
        for (String key : bank_data.keySet()) {
            total_coins += (Long) bank_data.get(key);
        }
        Long mean = total_coins/4;
        for (String key : bank_data.keySet()) {
            //TODO: Figure out calculation to do here
            double value = (mean.doubleValue()/((Long) bank_data.get(key)).doubleValue());
            double last_value = (double) sharedPref.getFloat(key, (float) value);
            double change = value - last_value;
            toAdd.add(new BankInfo(key,value,change));
            editor.putFloat(key,(float) value);
        }
        editor.commit();
        data.clear();
        data.addAll(toAdd);
        mAdapter.notifyDataSetChanged();
    }
    private void getBankDetails() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference dRef = database.collection("bank").document("totals");
        dRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //Get From memory
               updateBankWithDocumentSnapshot(documentSnapshot);
            }
        });
    }
}
