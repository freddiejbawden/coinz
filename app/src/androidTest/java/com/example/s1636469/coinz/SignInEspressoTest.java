package com.example.s1636469.coinz;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SignInEspressoTest {

    @Before
    public void setupTests(){
        Intents.init();
    }

    @Rule
    public ActivityTestRule<SignInActivity> mActivityRule =
            new ActivityTestRule<>(SignInActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Test
    public void userEntersAValidEmail() {
        onView(withId(R.id.email_edit_text))
                .perform(typeText(TestUtils.TEST_EMAIL),closeSoftKeyboard());
        onView(withId(R.id.password_edit_text))
                .perform(typeText(TestUtils.TEST_PASSWORD),closeSoftKeyboard());
        onView(withId(R.id.signin_button))
                .perform(click());
        SystemClock.sleep(5000);
        intended(hasComponent(MainActivity.class.getName()));

    }

    @Test
    public void userEntersAnEmailWithNoAccount() {
        onView(withId(R.id.email_edit_text))
                .perform(typeText(TestUtils.randomAlphaNumeric(6) + "@test.com"),closeSoftKeyboard());
        onView(withId(R.id.password_edit_text))
                .perform(typeText(TestUtils.TEST_PASSWORD),closeSoftKeyboard());
        onView(withId(R.id.signin_button))
                .perform(click());
        SystemClock.sleep(5000);

        onView(withId(R.id.email_edit_text))
                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.no_user_with_account))));
        onView(withId(R.id.login_progress))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void userEntersABadFormattedEmail() {

        onView(withId(R.id.email_edit_text))
                .perform(typeText("testtest.com"),closeSoftKeyboard());
        onView(withId(R.id.password_edit_text))
                .perform(typeText(TestUtils.TEST_PASSWORD),closeSoftKeyboard());
        onView(withId(R.id.signin_button))
                .perform(click());
        SystemClock.sleep(500);

        onView(withText(R.string.bad_email_password))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
        onView(withId(R.id.login_progress))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void userEntersABadFormattedPassword() {

        onView(withId(R.id.email_edit_text))
                .perform(typeText(TestUtils.TEST_EMAIL),closeSoftKeyboard());
        onView(withId(R.id.password_edit_text))
                .perform(typeText("1"),closeSoftKeyboard());
        onView(withId(R.id.signin_button))
                .perform(click());
        SystemClock.sleep(500);

        onView(withText(R.string.bad_email_password))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
        onView(withId(R.id.login_progress))
                .check(matches(not(isDisplayed())));
    }
    @Test
    public void userEntersNoEmail() {


        onView(withId(R.id.password_edit_text))
                .perform(typeText(TestUtils.TEST_PASSWORD),closeSoftKeyboard());
        onView(withId(R.id.signin_button))
                .perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.email_edit_text))
                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.no_email))));
        onView(withId(R.id.login_progress))
                .check(matches(not(isDisplayed())));
    }
    @Test
    public void userEntersNoPassword() {

        onView(withId(R.id.email_edit_text))
                .perform(typeText(TestUtils.TEST_EMAIL),closeSoftKeyboard());
        onView(withId(R.id.signin_button))
                .perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.password_edit_text))
                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.no_password))));
        onView(withId(R.id.login_progress))
                .check(matches(not(isDisplayed())));
    }

}
