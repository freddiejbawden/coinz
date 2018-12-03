/*
 *  BankFragment
 *
 *  Fragment displayed on the Main Activity that displays bank values and allows the user to deposit
 *  coins
 *
 *
 */

package com.example.s1636469.coinz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class BankFragment extends Fragment {


    private String TAG = "BankFragment";

    private BankValuesAdapter mAdapter;
    private ArrayList<BankInfo> data = new ArrayList<>();
    private View rootView;
    private Spinner cur_spinner;
    private EditText coin_amount;

    private double n_coins_submitted_today;
    private double user_cur_amount;

    @NonNull
    @Override
    public View onCreateView( @NonNull  LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.bank_fragment,container,false);

        // Set up recycler view
        RecyclerView mRecyclerView = rootView.findViewById(R.id.bank_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new BankValuesAdapter(getContext(), data);
        mRecyclerView.setAdapter(mAdapter);

        populateSpinner();
        getBankDetails();
        setUpListeners();
        return rootView;
    }

    /*
     *  populateSpinner
     *
     *  Adds menu items from menu resource to the currency selection spinner
     *
     */

    private void populateSpinner() {
        cur_spinner = rootView.findViewById(R.id.cur_spinner_bank);
        Context app_context = getContext();
        Button deposit = rootView.findViewById(R.id.submit_desposit);
        if (app_context == null) {
            Log.d(TAG, "Could not get app context");
            deposit.setEnabled(false);
            return;
        } else {
            deposit.setEnabled(true);
        }
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(app_context, R.array.currencies,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cur_spinner.setAdapter(adapter);
    }
    /*
     *   updateGoldValue
     *
     *   updates the gold value displayed below the deposit
     *
     */
    private void updateGoldValue(String value){
        if (!value.equals("") && !value.isEmpty()) {

            // Calculate the currency value
            String currency = (String) cur_spinner.getSelectedItem();
            double amount = Double.parseDouble(value);
            Activity a = getActivity();
            if (a != null) {
                SharedPreferences sharedPref= getActivity().getSharedPreferences("bank", 0);
                double val = (double) sharedPref.getFloat(currency,0);
                if (val != 0) {
                    // Update the text
                    TextView gold_value = rootView.findViewById(R.id.gold_value);
                    gold_value.setText("Gold Value: " + Config.round((amount*val),Config.CUR_VALUE_DP));
                }
            } else {
                Log.d(TAG, "activity null when trying to update gold value");
            }


        } else {
            // Set text to ?
            Log.d(TAG, "The value is empty");
            TextView gold_value = rootView.findViewById(R.id.gold_value);
            gold_value.setText("Gold Value: ?");
        }

    }

    /*
     *  setUpListeners
     *
     *  set up functions to fire when the UI is clicked
     *
     */
    private void setUpListeners() {

        //Get Reference to the Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference dRef = database.collection("bank").document("totals");

        // Listen for bank changes
        dRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                // Bank has changed
                Log.d(TAG,"Bank updated");
                if (documentSnapshot != null) {
                    updateBankWithDocumentSnapshot(documentSnapshot);
                } else {
                    Log.d(TAG, "Could not get bank update");
                }
            }
        });


        coin_amount = rootView.findViewById(R.id.coin_amount_bank);

        // Listen for when the spinner value is changed
        cur_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGoldValue(coin_amount.getText().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button submit_deposit =  rootView.findViewById(R.id.submit_desposit);
        submit_deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                onDepositClick(v);
                Log.d(TAG, "clicked");

            }
        });
    }

    /*
     *  updateBankWithDocumentSnapshot
     *
     *  update the bank values using data from the bank document
     *
     */
    private void updateBankWithDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Activity a = getActivity();
        if (a == null) {
            Log.d(TAG, "Activity was null when trying to update bank");
            return;
        }

        SharedPreferences sharedPref= getActivity().getSharedPreferences("bank", 0);
        SharedPreferences.Editor editor= sharedPref.edit();

        ArrayList<BankInfo> toAdd = new ArrayList<>();
        double total_coins = 0;

        Map<String, Object> bank_data = documentSnapshot.getData();
        if (bank_data == null) {
            Log.d(TAG, "Failed to update bank, bank_data was null");
            return;
        }

        //calculate and update the coin values
        for (String key : bank_data.keySet()) {
            try {
                total_coins += (Double) bank_data.get(key);
            } catch (ClassCastException e) {
                total_coins += ((Long) bank_data.get(key)).doubleValue();
            }
        }
        double mean = total_coins/4;

        for (String key : bank_data.keySet()) {

            double value;
            try {
               value = (mean/(Double) bank_data.get(key));
            } catch (ClassCastException e) {
                value = (mean/((Long) bank_data.get(key)).doubleValue());
            }

            double last_value = (double) sharedPref.getFloat(key, (float) value);
            double change = value - last_value;
            toAdd.add(new BankInfo(key,value,change));
            Log.d(TAG,"Change: " + change);
            editor.putFloat(key,(float) value);
        }
        editor.apply();

        //Update recycler
        data.clear();
        data.addAll(toAdd);
        mAdapter.notifyDataSetChanged();
    }

    /*
     *  getBankDetails
     *
     *  Pulls the bank details from Firestore
     */
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

    /*
     *  onDepositClick
     *
     *  Verify input and update bank as well as user documents
     *
     */
    private void onDepositClick(View v) {

        //Check input data
        String value = coin_amount.getText().toString();
        if (value.isEmpty()) {
            coin_amount.setError(getString(R.string.no_bank_deposit));
            coin_amount.requestFocus();
            v.setEnabled(true);
            return;
        }

        double amount = Double.parseDouble(coin_amount.getText().toString());
        if (amount <= 0) {
            coin_amount.setError(getString(R.string.negative_zero_depoist));
            coin_amount.requestFocus();
            v.setEnabled(true);
            return;
        }

        String cur = (String) cur_spinner.getSelectedItem();
        Activity a = getActivity();
        if (a == null) {
            Log.d(TAG, "Activity was null when depositing");
            v.setEnabled(true);
            return;
        }

        SharedPreferences sharedPref= getActivity().getSharedPreferences("bank", 0);
        double cur_val = (double) sharedPref.getFloat(cur,0);
        if (cur_val == 0){
            Toast.makeText(getContext(), String.format("Cannot find value for %s, deposit halted",cur),Toast.LENGTH_SHORT).show();
            v.setEnabled(true);
            return;
        }


        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "Failed to get authenticated user");
            v.setEnabled(true);
            return;
        }
        String id = auth.getCurrentUser().getUid();
        getValuesFromBankAndUpdate(database,id, cur,amount,cur_val,v);

    }

    /*
     *  Pull down the data from Firebase and update the user and bank
     */
    private void getValuesFromBankAndUpdate(FirebaseFirestore database, String id, String cur,
                                            double amount, double cur_val,View v) {
        DocumentReference uRef = database.collection("users").document(id);
        // Get the user's current gold value
        uRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                // Verify the user's input with the data from the server
                Map<String, Object> u_data = documentSnapshot.getData();
                if (u_data == null) {
                    Log.d(TAG,"user data was null");
                    Toast.makeText(getContext(), "Could not fetch user data",Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);
                    return;
                }
                try {
                    user_cur_amount = (Double) u_data.get(cur);
                } catch (ClassCastException e) {
                    user_cur_amount = ((Long) u_data.get(cur)).doubleValue();
                }

                try {
                    n_coins_submitted_today = (Double) u_data.get("coins_today");
                } catch (ClassCastException e) {
                    n_coins_submitted_today = ((Long) u_data.get("coins_today")).doubleValue();
                }
                if (n_coins_submitted_today >=  Config.TOTAL_COINS_PER_DAY) {
                    coin_amount.setError(getString(R.string.too_many_coins_today));
                    coin_amount.requestFocus();
                    v.setEnabled(true);
                    return;
                }
                if (n_coins_submitted_today + amount > Config.TOTAL_COINS_PER_DAY) {
                    coin_amount.setError(String.format(getString(R.string.depoist_overlimit),amount+""));
                    coin_amount.requestFocus();
                    v.setEnabled(true);
                    return;
                }
                if (amount > user_cur_amount) {
                    coin_amount.setError(getString(R.string.not_enough_coins));
                    coin_amount.requestFocus();
                    v.setEnabled(true);
                    return;
                }
                // Deposit Verified, update the bank and user documents
                DocumentReference bankRef = database.collection("bank").document("totals");
                bankRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> bank_data = documentSnapshot.getData();
                        if (bank_data == null ) {
                            Log.d(TAG,"bank data was null");
                            Toast.makeText(getContext(), "Could not deposit, could not get" +
                                    " bank information", Toast.LENGTH_SHORT).show();
                            v.setEnabled(true);
                            return;
                        }
                        updateBank(bank_data,cur,amount,bankRef);
                        updateUser(uRef, amount,cur,cur_val,v,u_data);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        v.setEnabled(true);
                        Log.w(TAG, "Failed to access to bank",e);
                        Toast.makeText(getContext(), "Cannot submit to the bank" +
                                "at this time",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                v.setEnabled(true);
                Log.w(TAG, "Failed to add to bank",e);
                Toast.makeText(getContext(), "Cannot submit to the bank" +
                        "at this time",Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
     *  updateUser
     *
     *  update the user's document with the new deposit
     *
     */
    private void updateUser(DocumentReference uRef, double amount, String cur, double cur_val,
                            View v, Map<String, Object> u_data) {

        double u_gold;
        try {
            u_gold = (Double) u_data.get("GOLD");
        } catch (ClassCastException e) {
            u_gold = ((Long) u_data.get("GOLD")).doubleValue();
        }

        double u_weekly_gold;
        try{
            u_weekly_gold = (Double) u_data.get("weekly_GOLD");
        } catch (ClassCastException e) {
            u_weekly_gold = ((Long) u_data.get("weekly_GOLD")).doubleValue();
        }
        // Calculate gold to add
        double gold_collected = amount*cur_val;
        u_weekly_gold += gold_collected;
        u_gold += gold_collected;

        // Update user
        HashMap<String, Object> user_update = new HashMap<>();
        user_update.put("coins_today",n_coins_submitted_today+amount);
        user_update.put(cur,user_cur_amount - amount);
        user_update.put("GOLD",u_gold);
        user_update.put("weekly_GOLD",u_weekly_gold);

        // Push changes to firestore
        uRef.set(user_update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), amount + " " + cur + " deposited as " +
                                Config.round(amount * cur_val, Config.CUR_VALUE_DP) + " GOLD",
                        Toast.LENGTH_SHORT).show();
                v.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                v.setEnabled(true);
                Log.w(TAG, "Failed to add to bank", e);
                Toast.makeText(getContext(), "Cannot submit to the bank" +
                        "at this time", Toast.LENGTH_SHORT).show();
                v.setEnabled(true);
            }
        });
    }
    /*
     *  updateBank
     *
     *  change bank counts with new values
     */
    private void updateBank(Map<String, Object> bank_data, String cur,double amount,
                            DocumentReference bankRef) {
        double cur_bank_total;
        try {
            cur_bank_total = (Double) bank_data.get(cur);
        } catch (ClassCastException e) {
            cur_bank_total = ((Long) bank_data.get(cur)).doubleValue();
        }
        cur_bank_total += amount;

        //Update Bank
        bank_data.put(cur,cur_bank_total);
        bankRef.set(bank_data, SetOptions.merge());
    }
}
