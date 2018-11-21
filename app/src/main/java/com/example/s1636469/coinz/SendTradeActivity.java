package com.example.s1636469.coinz;

import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SendTradeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_trade);

        Spinner cur_spinner = (Spinner) findViewById(R.id.cur_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cur_spinner.setAdapter(adapter);
        setUpListeners();
    }
    private void setUpListeners() {
        FloatingActionButton send = findViewById(R.id.confirm_trade);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText recp = (EditText) findViewById(R.id.recipient_inp);
                String other_user = recp.getText().toString();
                EditText str_amount = (EditText) findViewById(R.id.coin_amount);
                double amount = Double.parseDouble(str_amount.getText().toString());
                Spinner cur_spin = findViewById(R.id.cur_spinner);
                String cur = (String) cur_spin.getSelectedItem();
                Log.d("Trade",other_user);
                Log.d("Trade",amount + "");
                Log.d("Trade",cur);
                // check the sender has enough coinz
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                DocumentReference user_ref = database.collection("users").document("initial");
                user_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> user_data = documentSnapshot.getData();
                        String cur_amount_str = (String) user_data.get(cur);
                        double cur_amount = Double.parseDouble(cur_amount_str);
                        if (cur_amount >= amount) {
                            //Trade is good to go!
                            HashMap<String, Object> to_put_user = new HashMap<>();
                            to_put_user.put(cur, (cur_amount-amount) + "");

                            DocumentReference other_ref = database.collection("users").document(other_user);
                            other_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Map<String, Object> other_data = documentSnapshot.getData();
                                    String other_u_amount_str = (String) other_data.get(cur);
                                    double other_u_amount = Double.parseDouble(other_u_amount_str);
                                    HashMap<String, Object> to_put_other = new HashMap<>();
                                    to_put_other.put(cur, (other_u_amount+amount) + "");
                                    user_ref.set(to_put_user, SetOptions.merge());
                                    other_ref.set(to_put_other, SetOptions.merge());
                                    Toast.makeText(getApplicationContext(), amount + " "  + cur + " sent to " + other_user,Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Cannot find user " + other_user,Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            //User does not have enough cashola
                            String out = "You don't have enough " + cur + " to perform this transaction! Check your wallet!";
                            Toast.makeText(getApplicationContext(), out ,Toast.LENGTH_LONG).show();
                        }
                    }
                });
                    //if so; get the other users wallet and send the coinz to it
                    //if not; display a toast and hopefully don't crash
            }
        });

        ImageView close_button = (ImageView) findViewById(R.id.close_button);

        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
