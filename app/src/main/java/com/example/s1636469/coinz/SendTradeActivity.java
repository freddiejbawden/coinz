/*
 *  SendTradeActivity
 *
 *  Allows user to send coinz to another player
 *
 */

package com.example.s1636469.coinz;

import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendTradeActivity extends Activity {

    private ProgressBar progressBar;
    private String TAG = "SendTrade";
    private  EditText recp;
    private EditText amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_trade);

        // Set up currency spinner
        Spinner cur_spinner =  findViewById(R.id.cur_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cur_spinner.setAdapter(adapter);

        // Set up progress bar
        progressBar = findViewById(R.id.send_coins_progress);
        progressBar.setVisibility(View.INVISIBLE);

        setUpListeners();
    }
    /*
     *  setUpListeners
     *
     *  sets up listeners for all buttons in the UI
     */
    private void setUpListeners() {
        FloatingActionButton send = findViewById(R.id.confirm_trade);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTradeInput();
            }
        });

        ImageView close_button = findViewById(R.id.close_button);

        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /*
     *  checkTradeInput
     *
     *  Verifies that the input in the EditText fields are valid and passes them to be
     *  checked against Firebase info
     */
    private void checkTradeInput() {
        progressBar.setVisibility(View.VISIBLE);
        recp = findViewById(R.id.recipient_inp);
        String other_user = recp.getText().toString();
        amount =  findViewById(R.id.coin_amount);

        if (other_user.isEmpty()) {
            recp.setError(getString(R.string.no_trade_user));
            recp.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        String str_amount = amount.getText().toString();
        if (str_amount.isEmpty()) {
            amount.setError(getString(R.string.no_trade_amount));
            amount.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        double trade_amount = Double.parseDouble(str_amount);
        if (trade_amount <= 0) {
            amount.setError(getString(R.string.negative_zero_trade_amount));
            amount.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }


        Spinner cur_spin = findViewById(R.id.cur_spinner);
        String cur = (String) cur_spin.getSelectedItem();
        Log.d(TAG,other_user);
        Log.d(TAG,amount + "");
        Log.d(TAG,cur);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getApplicationContext(), "Cannot send trade at the moment",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Firebase User was null");
            return;
        }
        String id = auth.getCurrentUser().getUid();

        // Get Firebase reference
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference user_ref = database.collection("users").document(id);
        // Pass data to next verification step
        user_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                verifyTradeWithUser(documentSnapshot,cur, trade_amount, database, other_user,
                        user_ref, id);
            }
        });
    }
    /*
     *  verifyTradeWithUser
     *
     *  Verify that the user has enough coins to send to the other player then pass to check
     *  if recieving user exists
     *
     */
    private void verifyTradeWithUser(DocumentSnapshot documentSnapshot, String cur, double trade_amount,
                                     FirebaseFirestore database, String other_user,
                                     DocumentReference user_ref, String id) {
        Map<String, Object> user_data = documentSnapshot.getData();
        if (user_data == null) {
            Toast.makeText(getApplicationContext(), "Cannot send trade at the " +
                    "moment", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Firebase user was null");
            return;
        }
        Double cur_amount;
        try {
            cur_amount =(Double) user_data.get(cur);
        } catch (ClassCastException e) {
            cur_amount = ((Long) user_data.get(cur)).doubleValue();
        }

        Log.d(TAG, "Cur Amount: " + cur_amount);

        if (cur_amount < trade_amount) {
            //User does not have enough coins
            amount.setError(getString(R.string.no_enough_for_trades));
            amount.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //Trade is good to go!
        final String user_name = (String) user_data.get("name");

        HashMap<String, Object> to_put_user = new HashMap<>();
        to_put_user.put(cur, (cur_amount-trade_amount));

        Query query = database.collection("users").whereEqualTo("name",other_user);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                verifyOtherUser(queryDocumentSnapshots, cur, database, trade_amount,
                        user_ref, to_put_user, id, user_name, other_user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Cannot find user " + other_user,Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    /*
     *  verifyOtherUser
     *
     *  Check if the recieving user exists, if so send the trade and update the history
     */
    private void verifyOtherUser(QuerySnapshot queryDocumentSnapshots, String cur, FirebaseFirestore database,
                                double trade_amount, DocumentReference user_ref,
                                HashMap<String, Object> to_put_user, String id, String user_name,
                                 String other_user) {

        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();

        // Check if the user exists
        if (documentSnapshots.isEmpty()) {
            recp.setError(getString(R.string.user_not_found));
            recp.requestFocus();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        Map<String, Object> other_data = documentSnapshots.get(0).getData();
        if (other_data == null) {
            Toast.makeText(getApplicationContext(), "Cannot send trade at the " +
                    "moment", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Firebase user was null");
            return;
        }
        String other_u_id = documentSnapshots.get(0).getId();

        // Build map to push to other user
        Double other_u_amount;
        try {
            other_u_amount = (Double) other_data.get(cur);
        } catch (ClassCastException e) {
            other_u_amount = ((Long) other_data.get(cur)).doubleValue();
        }

        final String other_name = (String) other_data.get("name");

        DocumentReference other_ref = database.collection("users")
                .document(other_u_id);

        HashMap<String, Object> to_put_other = new HashMap<>();
        // Send to other user
        to_put_other.put(cur, (other_u_amount+trade_amount));

        // Update user's document
        user_ref.set(to_put_user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                other_ref.set(to_put_other, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), trade_amount + " "  + cur + " sent to " + other_user,Toast.LENGTH_LONG).show();
                        updateTradeDataArray(id, user_name, other_u_id, other_name,trade_amount,cur);
                    }
                });
            }
        });
    }

    /*
     *  updateTradeDataArray
     *
     *  Update both player's trade array
     */
    private void updateTradeDataArray(String id, String name, String o_user_id,String other_name, double amount, String cur_type) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        TradeData for_us = new TradeData(true,name,other_name,amount+"",cur_type);
        TradeData for_them = new TradeData(false,other_name,name,amount+"",cur_type);


        // Update the user's trade array
        CollectionReference c_ref = database.collection("users");
        c_ref.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> data = documentSnapshot.getData();
                if (data == null ) {
                    Toast.makeText(getApplicationContext(), "Cannot update trade history",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Data was null");
                }
                // Add another entry and push to firebase
                List<Object> trades = (List<Object>) documentSnapshot.getData().get("trades");
                trades.add(for_us.to_map());
                HashMap<String, Object> to_put = new HashMap<String, Object>() {{
                    put("trades",trades);
                }};
                c_ref.document(id).set(to_put, SetOptions.merge());

            }
        });

        // Update the other player's trade array
        c_ref.document(o_user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> data = documentSnapshot.getData();
                if (data == null ) {
                    Toast.makeText(getApplicationContext(), "Cannot update trade history",
                            Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Data was null");
                    return;
                }
                // Add another entry and push to firebase

                List<Object> trades = (List<Object> ) data.get("trades");
                trades.add(for_them.to_map());
                HashMap<String, Object> to_put = new HashMap<String, Object>() {{
                    put("trades",trades);
                }};
                c_ref.document(o_user_id).set(to_put, SetOptions.merge());
            }
        });
    }
}
