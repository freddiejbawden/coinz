package com.example.s1636469.coinz;


import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.onData;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.CoreMatchers.anything;

@RunWith(AndroidJUnit4.class)
public class BankFragmentEspressoTest {

    @Before
    public void setUpBank() {
        mActivityRule.getActivity();
        NoSwipingViewPager viewPager = mActivityRule.getActivity().findViewById(R.id.container);
        viewPager.setCurrentItem(2);



    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    @After
    public void resetUser() {
        TestUtils.resetUser();
    }

    @Test
    public void userInputsNegativeCoins() {
        FirebaseAuth auth  =FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(TestUtils.TEST_EMAIL, TestUtils.TEST_PASSWORD)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        mActivityRule.getActivity();
                        NoSwipingViewPager viewPager = mActivityRule.getActivity().findViewById(R.id.container);
                        viewPager.setCurrentItem(2);
                        SystemClock.sleep(500);
                        onView(withId(R.id.coin_amount_bank)).perform(typeText("-1"));

                        onView(withId(R.id.submit_desposit)).perform(click());

                        SystemClock.sleep(500);

                        onView(withId(R.id.coin_amount_bank))
                                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.negative_zero_depoist))));
                    }
                });
    }
    @Test
    public void noCoinsInputted() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(TestUtils.TEST_EMAIL, TestUtils.TEST_PASSWORD)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        NoSwipingViewPager viewPager = mActivityRule.getActivity().findViewById(R.id.container);
                        viewPager.setCurrentItem(2);
                        SystemClock.sleep(500);

                        onView(withId(R.id.submit_desposit)).perform(click());

                        SystemClock.sleep(500);

                        onView(withId(R.id.coin_amount_bank))
                                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.no_bank_deposit))));
                    }
                });
    }
    //@Test
//    public void tryAllCoins() {
//        FirebaseAuth auth  =FirebaseAuth.getInstance();
//        auth.signInWithEmailAndPassword(TestUtils.TEST_EMAIL, TestUtils.TEST_PASSWORD)
//                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                    @Override
//                    public void onSuccess(AuthResult authResult) {
//                        mActivityRule.getActivity();
//                        NoSwipingViewPager viewPager = mActivityRule.getActivity().findViewById(R.id.container);
//                        viewPager.setCurrentItem(2);
//                        SystemClock.sleep(500);
//                        onView(withId(R.id.coin_amount_bank)).perform(typeText("1"));
//
//                        FirebaseFirestore database =FirebaseFirestore.getInstance();
//                        database.collection("bank").document("values").get()
//                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                        Map<String, Object> data = documentSnapshot.getData();
//                                         for (String c : Config.currencies) {
//                                             onView(withId(R.id.coin_amount_bank)).perform(typeText("1"));
//                                             onView(withId(R.id.cur_spinner)).perform(click());
//                                             onData(allOf(is(instanceOf(String.class)), is("PENY"))).perform(click());
//                                             onView(withId(R.id.cur_spinner)).check(matches(withSpinnerText(containsString("PENY"))));
//
//                                             onView(withId(R.id.submit_desposit)).perform(click());
//
//                                             SystemClock.sleep(5000);
//
//                                             double exchange_rate;
//                                             try {
//                                                 exchange_rate = (Double) data.get(c);
//                                             } catch (ClassCastException e) {
//                                                 exchange_rate = ((Long) data.get(c)).doubleValue();
//                                             }
//                                             EditText c_amount = (EditText) mActivityRule.getActivity().findViewById(R.id.coin_amount_bank);
//                                             double amount = Double.parseDouble(c_amount.getText().toString());
//                                             String expected_text = String.format(amount + " " + c + " deposited as " + amount*exchange_rate);
//                                             onView(withText(expected_text))
//                                                     .inRoot(withDecorView(not(Matchers.is(mActivityRule.getActivity().getWindow().getDecorView()))))
//                                                     .check(matches(isDisplayed()));
//
//                                         }
//                                    }
//                                });
//                    }
//                });
//        }
}
