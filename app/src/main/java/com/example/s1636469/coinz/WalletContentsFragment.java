/*
 * WalletContentsFragment
 *
 * Allows the user to view how many of each type of coin they have
 */
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Map;

public class WalletContentsFragment extends Fragment {

    private WalletContentsAdapter mAdapter;
    private ArrayList<WalletCurrency> data = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.wallet_contents_recycler, container, false);

        // Set up Recycler View
        RecyclerView mRecyclerView= rootView.findViewById(R.id.currency_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new WalletContentsAdapter(getContext(),data);
        mRecyclerView.setAdapter(mAdapter);

        setUpListeners();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Cannot get wallet data at the moment",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String id = auth.getCurrentUser().getUid();
        getWalletData(id);
    }
    /*
     * getWalletData
     *
     *  get wallet data for the user
     */
    protected void getWalletData(String username) {

        // Get reference to Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        final DocumentReference docRef = database.collection("users").document(username);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult() != null && task.getResult().exists()) {
                    // display wallet data
                    displayWalletFromSnapshot(task.getResult());
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

    /*
     * displayWalletFromSnapshot
     *
     * displays wallet information from a document snapshot
     *
     */
    private void displayWalletFromSnapshot(DocumentSnapshot documentSnapshot) {
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

            // add row to the recycler
            toAdd.add(new WalletCurrency(cur, value));
        }
        // Update recycler row
        data.clear();
        data.addAll(toAdd);
        mAdapter.notifyDataSetChanged();
    }
    /*
     * setUpListeners
     *
     * sets up a snapshot listener for the user's wallet
     */
    private void setUpListeners() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Cannot get wallet data at the moment",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String id = user.getUid();
        DocumentReference dRef = database.collection("users").document(id);
        dRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                displayWalletFromSnapshot(documentSnapshot);
            }
        });
    }
}
