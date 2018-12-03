/*
 * TradeFragment
 *
 * Allows user to open trade sending activity and view past trades
 */
package com.example.s1636469.coinz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;

public class TradesFragment extends Fragment {


    public TradeAdapter mTradeAdapter;
    private ArrayList<TradeData> data = new ArrayList<>();
    private String TAG = "TradesFragment";
    private FloatingActionButton send_coins;
    private ProgressBar progressBar;
    private TextView failText;


    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trades,container,false);

        // Set up recycler view
        RecyclerView mRecyclerView = rootView.findViewById(R.id.recycler_trade_history);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTradeAdapter = new TradeAdapter(getContext(), data);
        mRecyclerView.setAdapter(mTradeAdapter);

        // Set up rest of the UI
        send_coins = rootView.findViewById(R.id.send_trade_button);

        failText = rootView.findViewById(R.id.no_trades_text);
        failText.setVisibility(View.INVISIBLE);

        progressBar = rootView.findViewById(R.id.trades_progress);
        progressBar.setVisibility(View.INVISIBLE);

        return rootView;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpListeners();
        getTrades();
    }

    @Override
    public void onResume() {
        super.onResume();
        getTrades();
    }
    /*
     *  setUpListeners
     *
     *  sets up listener for Floating Action Button
     */
    private void setUpListeners() {
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

    /*
     *  getTrades
     *
     *  gets the trade history of the user and displays it
     */
    private void getTrades() {

        failText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);


        // Get Firebase reference
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "Cannot get trades at the moment",Toast.LENGTH_SHORT).show();
            failText.setVisibility(View.VISIBLE);
            return;
        }
        String id = auth.getCurrentUser().getUid();
        DocumentReference dRef = database.collection("users").document(id);

        dRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Map<String, Object> f_store_data = documentSnapshot.getData();
                List<Object> trades = (List<Object>) f_store_data.get("trades");

                //Check if the user has trades
                if (trades.isEmpty()) {
                    failText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                ArrayList<TradeData> toAdd = new ArrayList<>();
                Log.d(TAG,trades.size() + "");
                String u_name = (String) f_store_data.get("name");

                // Display details of the all the trades
                for (Object t : trades) {
                    Map<String, Object> trade_data = (Map<String, Object>) t;
                    boolean sent_by_user = (boolean) trade_data.get("sent_by_user");
                    String other_user = (String) trade_data.get("other_user");
                    Double value;
                    try {
                        value = (Double) trade_data.get("amount");
                    } catch (ClassCastException e) {
                        value =((Long) trade_data.get("amount")).doubleValue();
                    }
                    String amount = value.toString();
                    String cur =   (String) trade_data.get("currency");
                    toAdd.add(new TradeData(sent_by_user, u_name, other_user, amount,cur));
                }

                // Update recycler view
                data.clear();
                data.addAll(toAdd);
                progressBar.setVisibility(View.INVISIBLE);
                mTradeAdapter.notifyDataSetChanged();
            }
        });
    }
}
