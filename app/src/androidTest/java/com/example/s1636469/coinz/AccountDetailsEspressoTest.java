package com.example.s1636469.coinz;


import android.content.Intent;
import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import static android.support.test.espresso.Espresso.onView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Set;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class AccountDetailsEspressoTest {

    @Rule
    public ActivityTestRule<SetUpAccountActivity> mActivityRule =
            new ActivityTestRule<>(SetUpAccountActivity.class);

    @Before
    public void startActivity() {
        Intents.init();
        mActivityRule.launchActivity(new Intent());
    }

    @Test
    public void userDoesNotProvideADisplayName() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(TestUtils.TEST_EMAIL, TestUtils.TEST_PASSWORD)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //This makes the thing wait until we are logged in
                        onView(withId(android.R.id.content)).perform(ViewActions.click());

                        onView(withId(R.id.continue_to_app)).perform(click());

                        onView(withId(R.id.display_name))
                                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.no_username))));
                    }
                });

    }

    @Test
    public void usernameIsTaken() {
        mActivityRule.launchActivity(new Intent());
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(TestUtils.TEST_EMAIL, TestUtils.TEST_PASSWORD)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //This makes the thing wait until we are logged in
                        onView(withId(R.id.display_name)).perform(typeText(TestUtils.TEST_USERNAME));

                        onView(withId(R.id.continue_to_app)).perform(click());

                        onView(withId(R.id.display_name))
                                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.username_invalid))));
                    }
                });
    }

    @Test
    public void userDoesNotPickAPicture() {
        mActivityRule.launchActivity(new Intent());
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(TestUtils.TEST_EMAIL, TestUtils.TEST_PASSWORD)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //This makes the thing wait until we are logged in
                        onView(withId(R.id.display_name)).perform(typeText(TestUtils.randomAlphaNumeric(6)));

                        onView(withId(R.id.continue_to_app)).perform(click());

                        SystemClock.sleep(5000);

                        intended(hasComponent(MainActivity.class.getName()));

                        String id = auth.getCurrentUser().getUid();
                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        HashMap<String, Object> reset = new HashMap<String, Object>();
                        reset.put("name","testuser");
                        database.collection("users").document(id).set(reset, SetOptions.merge());
                    }
                });
    }

}
