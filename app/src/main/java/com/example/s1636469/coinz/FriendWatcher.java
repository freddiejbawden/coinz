/*
 *  Friend Watcher
 *
 *  Runnable class that updates the user's friend collection with new values
 */

package com.example.s1636469.coinz;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendWatcher implements Runnable {

    protected static boolean STOP_FLAG = false;
    private String id;
    private String TAG = "FriendWatcher";
    private Thread thread;

    protected FriendWatcher(String id, Thread t) {
        this.id = id;
        this.thread = t;
    }

    private void updateGold(List<DocumentSnapshot> documentSnapshotList, FirebaseFirestore database,
                            CollectionReference user_f_ref) {
        if (!documentSnapshotList.isEmpty()) {
            // get the head of the list
            DocumentSnapshot current_doc = documentSnapshotList.get(0);
            final String current_id = current_doc.getId();

            // get the friend's document
            DocumentReference d_ref = database.collection("users").document(current_id);
            d_ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Map<String, Object> friend_data = documentSnapshot.getData();
                    Double new_gold;
                    if (friend_data == null) {
                        Log.d(TAG, "Unable to get friend data!");
                        documentSnapshotList.remove(0);
                        updateGold(documentSnapshotList,database,user_f_ref);
                        return;
                    }
                    try {
                        new_gold = (Double) friend_data.get("GOLD");
                    } catch (ClassCastException e) {
                        new_gold = ((Long) friend_data.get("GOLD")).doubleValue();
                    }

                    // Update the user's collection
                    HashMap<String, Object> to_put = new HashMap<String, Object>();
                    to_put.put("GOLD",new_gold);
                    user_f_ref.document(current_id).set(to_put, SetOptions.merge());
                    documentSnapshotList.remove(0);
                    updateGold(documentSnapshotList,database,user_f_ref);
                }
            });
        }


    }
    @Override
    public void run() {

        while (STOP_FLAG) {
            Log.d(TAG,"Updating");
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            final CollectionReference u_friends = database.collection("users").document(id).collection("friends");

            // get the users' friend collection
            u_friends.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();

                    // update their values
                    updateGold(documentSnapshots, database,u_friends);
                }
            });
            try {
                // Sleep for 10 seconds
                thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.d(TAG, "Interupted");
            }
        }
    }
}
