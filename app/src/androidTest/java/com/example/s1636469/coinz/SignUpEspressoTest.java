package com.example.s1636469.coinz;


import android.os.SystemClock;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


@RunWith(AndroidJUnit4.class)
public class SignUpEspressoTest {

    @Before
    public void setupTests(){
        Intents.init();
    }

    @Rule
    public ActivityTestRule<SignInActivity> mActivityRule =
            new ActivityTestRule<>(SignInActivity.class);

    @Test
    public void userEntersAValidEmailAndPassword() {
        String u_name = TestUtils.randomAlphaNumeric(7);
        String email = String.format("%s@test.com",u_name);

        onView(withId(R.id.email_edit_text))
                .perform(typeText(email),closeSoftKeyboard());
        onView(withId(R.id.password_edit_text))
                .perform(typeText("test_password"),closeSoftKeyboard());
        onView(withId(R.id.signup_button))
                .perform(click());
        SystemClock.sleep(5000);
        intended(hasComponent(SetUpAccountActivity.class.getName()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser u  = auth.getCurrentUser();
        u.delete();

    }
    @Test
    public void userEntersABadFormattedEmail() {

        onView(withId(R.id.email_edit_text))
                .perform(typeText("testtest.com"),closeSoftKeyboard());
        onView(withId(R.id.password_edit_text))
                .perform(typeText("test_password"),closeSoftKeyboard());
        onView(withId(R.id.signup_button))
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
                .perform(typeText("test@test.com"),closeSoftKeyboard());
        onView(withId(R.id.password_edit_text))
                .perform(typeText("1"),closeSoftKeyboard());
        onView(withId(R.id.signup_button))
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
                .perform(typeText("test_password"),closeSoftKeyboard());
        onView(withId(R.id.signup_button))
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
                .perform(typeText("test@test.com"),closeSoftKeyboard());
        onView(withId(R.id.signup_button))
                .perform(click());
        SystemClock.sleep(500);

        onView(withId(R.id.password_edit_text))
                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.no_password))));
        onView(withId(R.id.login_progress))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void userEntersAUserNameThatIsAlreadyTaken() {
        onView(withId(R.id.email_edit_text))
                .perform(typeText("test@test.com"),closeSoftKeyboard());
        onView(withId(R.id.password_edit_text))
                .perform(typeText("test_password"),closeSoftKeyboard());
        onView(withId(R.id.signup_button))
                .perform(click());

        SystemClock.sleep(500);

        onView(withId(R.id.email_edit_text))
                .check(matches(hasErrorText(mActivityRule.getActivity().getString(R.string.email_taken))));
        onView(withId(R.id.login_progress))
                .check(matches(not(isDisplayed())));
    }
}
