package com.example.s1636469.coinz;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class TestUtils {

    protected static String TEST_EMAIL = "test@test.com";
    protected static String TEST_PASSWORD = "test_password";
    protected static String TEST_USERNAME = "testuser";

    //From https://dzone.com/articles/generate-random-alpha-numeric
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    protected static void resetUser() {
        HashMap<String, Object> reset = new HashMap<String, Object>() {{
            put("name","testuser");
            put("PENY",10);
            put("SHIL",10);
            put("QUID",10);
            put("DOLR",10);
            put("coins_today",0);
            put("trades",new ArrayList<Object>());
        }};

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection("users").document(id).set(reset, SetOptions.merge()).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TestUtils", "Failed to reset", e);
            }
        });
    }
}

