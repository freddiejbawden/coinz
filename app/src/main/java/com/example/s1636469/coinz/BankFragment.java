package com.example.s1636469.coinz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.firebase.firestore.CollectionReference;
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

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;

public class BankFragment extends Fragment {


    private String TAG = "BankFragment";

    private RecyclerView mRecyclerView;
    private BankValuesAdapter mAdapter;
    private ArrayList<BankInfo> data = new ArrayList<>();
    private View rootView;
    private Spinner cur_spinner;
    private EditText coin_amount;

    private double n_coins_submitted_today;
    private double user_cur_amount;
    @Override
    @NonNull
    public View onCreateView(LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        rootView =  inflater.inflate(R.layout.bank_fragment,container,false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.bank_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new BankValuesAdapter(getContext(), data);
        mRecyclerView.setAdapter(mAdapter);
        populateSpinner();
        getBankDetails();
        setUpListeners();
        return rootView;
    }
    private void populateSpinner() {
        cur_spinner = (Spinner) rootView.findViewById(R.id.cur_spinner_bank);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.currencies,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cur_spinner.setAdapter(adapter);
    }
    private void updateGoldValue(String value){
        if (!value.equals(null) && !value.equals("") && !value.isEmpty()) {
            String currency = (String) cur_spinner.getSelectedItem();
            double amount = Double.parseDouble(value);
            SharedPreferences sharedPref= getActivity().getSharedPreferences("bank", 0);
            double val = (double) sharedPref.getFloat(currency,0);
            if (val == 0) {
                // Do something
            } else {
                TextView gold_value = rootView.findViewById(R.id.gold_value);
                gold_value.setText("Gold Value: " + Config.round((amount*val),Config.CUR_VALUE_DP));
            }
        } else {
            Log.d(TAG, "something has gone wrong");
        }

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

        coin_amount = rootView.findViewById(R.id.coin_amount_bank);
        coin_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateGoldValue(s.toString());
            }
        });

        cur_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGoldValue(coin_amount.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button submit_deposit = (Button) rootView.findViewById(R.id.submit_desposit);
        submit_deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                onDepositClick(v);
            }
        });
    }
    private void updateBankWithDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        SharedPreferences sharedPref= getActivity().getSharedPreferences("bank", 0);
        SharedPreferences.Editor editor= sharedPref.edit();

        ArrayList<BankInfo> toAdd = new ArrayList<>();

        double total_coins = 0;
        Map<String, Object> bank_data = documentSnapshot.getData();
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

    private void onDepositClick(View v) {
        String value = coin_amount.getText().toString();
        if (!value.isEmpty()) {
            double amount = Double.parseDouble(coin_amount.getText().toString());;
            String cur = (String) cur_spinner.getSelectedItem();
            SharedPreferences sharedPref= getActivity().getSharedPreferences("bank", 0);
            double cur_val = (double) sharedPref.getFloat(cur,0);
            if (cur_val == 0){
                Toast.makeText(getContext(), String.format("Cannot find value for %s, deposit halted",cur),Toast.LENGTH_SHORT).show();
            } else {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String id = auth.getCurrentUser().getUid();
                DocumentReference uRef = database.collection("users").document(id);
                uRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> u_data = documentSnapshot.getData();

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

                        if (n_coins_submitted_today + amount > Config.TOTAL_COINS_PER_DAY) {
                            double coins_still_to_deposit = Config.TOTAL_COINS_PER_DAY-n_coins_submitted_today;
                            Toast.makeText(getContext(),
                                    "You have already submitted " + n_coins_submitted_today+ " coins today!",
                                    Toast.LENGTH_LONG).show();
                        } else if (amount > user_cur_amount) {
                            Toast.makeText(getContext(),
                                    String.format("You do not have enough %s in your wallet to deposit!",cur),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            //deposit okay
                            DocumentReference bankRef = database.collection("bank").document("totals");
                            bankRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Map<String, Object> bank_data = documentSnapshot.getData();

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

                                    //Update User
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

                                    double gold_collected = amount*cur_val;
                                    u_weekly_gold += gold_collected;
                                    u_gold += gold_collected;

                                    HashMap<String, Object> user_update = new HashMap<String, Object>();
                                    user_update.put("coins_today",n_coins_submitted_today+amount);
                                    user_update.put(cur,user_cur_amount - amount);
                                    user_update.put("GOLD",u_gold);
                                    user_update.put("weekly_GOLD",u_weekly_gold);

                                    uRef.set(user_update, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(), amount+" "+cur+" deposited as " +
                                                    Config.round(amount*cur_val,Config.CUR_VALUE_DP) + " GOLD",
                                                    Toast.LENGTH_SHORT).show();
                                            v.setEnabled(true);
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


        }
    }

}
