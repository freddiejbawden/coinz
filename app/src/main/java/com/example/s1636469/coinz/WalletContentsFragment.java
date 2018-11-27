package com.example.s1636469.coinz;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.ArrayList;
import java.util.Map;

public class WalletContentsFragment extends Fragment {
    private View rootView;
    private RecyclerView mRecyclerView;
    private WalletContentsAdapter mAdapter;
    private ArrayList<WalletCurrency> data = new ArrayList<WalletCurrency>();
    private String TAG = "WalletContents";



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.wallet_contents_recycler, container, false);
        // 1. get a reference to recyclerView
        mRecyclerView= (RecyclerView) rootView.findViewById(R.id.currency_recycler);

        // 2. set layoutManger
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new WalletContentsAdapter(getContext(),data);
        // 4. set adapter
        mRecyclerView.setAdapter(mAdapter);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();
        getWalletData(id);
        setUpListeners();
        return rootView;
    }

    protected void getWalletData(String username) {
        FirebaseFirestore.setLoggingEnabled(false);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        database.setFirestoreSettings(settings);

        final DocumentReference docRef = database.collection("users").document(username);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    getWalletFromSnapshot(task.getResult());
                } else {
                    Log.w("STATUS", "Document does not exist!");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("STATUS", "Failed to get user:" + username + " details");
            }
        });
    }


    private void getWalletFromSnapshot(DocumentSnapshot documentSnapshot) {
        ArrayList<WalletCurrency> toAdd = new ArrayList<WalletCurrency>();
        Log.d("STATUS",documentSnapshot.getData().toString());
        Map<String, Object> docSnap = documentSnapshot.getData();

        for (String cur : Config.currencies) {
            Double value;
            try {
                value = (Double) docSnap.get(cur);
            } catch ( ClassCastException e) {
                value = ((Long) docSnap.get(cur)).doubleValue();
            }

            toAdd.add(new WalletCurrency(cur, value));
        }

        data.clear();
        data.addAll(toAdd);
        mAdapter.notifyDataSetChanged();
    }

    private void setUpListeners() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();
        DocumentReference dRef = database.collection("users").document(id);
        dRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                getWalletFromSnapshot(documentSnapshot);
            }
        });
    }
}
