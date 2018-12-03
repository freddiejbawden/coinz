package com.example.s1636469.coinz;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kxml2.kdom.Document;

import java.util.HashMap;
import java.util.Set;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasErrorText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


@RunWith(AndroidJUnit4.class)
public class BankDespoistEspressoTest {

    @Rule
    public ActivityTestRule<SignInActivity> mActivityTestRule = new ActivityTestRule<>(SignInActivity.class);


    @After
    public void reset() {
        TestUtils.resetUser();
    }

    @Test
    public void userDoesntSetAnyCoinz() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html


        ViewInteraction editText = onView(
                allOf(withId(R.id.email_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                5),
                        isDisplayed()));
        editText.perform(replaceText("test@test.com"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.password_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                4),
                        isDisplayed()));
        editText2.perform(replaceText("test_password"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button = onView(
                allOf(withId(R.id.signin_button), withText("Sign In"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                2),
                        isDisplayed()));
        button.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.action_bank), withContentDescription("Bank"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomNavigationView),
                                        0),
                                2),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.submit_desposit), withText("Deposit"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.container),
                                        1),
                                5),
                        isDisplayed()));
        appCompatButton.perform(click());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            onView(withId(R.id.coin_amount_bank)).check(matches(hasErrorText(mActivityTestRule.getActivity().getString(R.string.no_bank_deposit))));
        }

    }
    @Test
    public void userDeposits0Coins() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html


        ViewInteraction editText = onView(
                allOf(withId(R.id.email_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                5),
                        isDisplayed()));
        editText.perform(replaceText("test@test.com"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.password_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                4),
                        isDisplayed()));
        editText2.perform(replaceText("test_password"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button = onView(
                allOf(withId(R.id.signin_button), withText("Sign In"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                2),
                        isDisplayed()));
        button.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.action_bank), withContentDescription("Bank"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomNavigationView),
                                        0),
                                2),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        onView(withId(R.id.coin_amount_bank)).perform(typeText("0"));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.submit_desposit), withText("Deposit"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.container),
                                        1),
                                5),
                        isDisplayed()));
        appCompatButton.perform(click());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }

        onView(withId(R.id.coin_amount_bank)).check(matches(hasErrorText(mActivityTestRule.getActivity().getString(R.string.negative_zero_depoist))));

    }
    @Test
    public void userDepositsNegativeCoins() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html


        ViewInteraction editText = onView(
                allOf(withId(R.id.email_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                5),
                        isDisplayed()));
        editText.perform(replaceText("test@test.com"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.password_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                4),
                        isDisplayed()));
        editText2.perform(replaceText("test_password"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button = onView(
                allOf(withId(R.id.signin_button), withText("Sign In"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                2),
                        isDisplayed()));
        button.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.action_bank), withContentDescription("Bank"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomNavigationView),
                                        0),
                                2),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        onView(withId(R.id.coin_amount_bank)).perform(typeText("-1"));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.submit_desposit), withText("Deposit"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.container),
                                        1),
                                5),
                        isDisplayed()));
        appCompatButton.perform(click());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }

        onView(withId(R.id.coin_amount_bank)).check(matches(hasValueEqualTo("1")));

    }
    @Test
    public void userDespositsMoreCoinsThanTheyHave() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html


        ViewInteraction editText = onView(
                allOf(withId(R.id.email_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                5),
                        isDisplayed()));
        editText.perform(replaceText("test@test.com"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction editText2 = onView(
                allOf(withId(R.id.password_edit_text),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                4),
                        isDisplayed()));
        editText2.perform(replaceText("test_password"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction button = onView(
                allOf(withId(R.id.signin_button), withText("Sign In"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                2),
                        isDisplayed()));
        button.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.action_bank), withContentDescription("Bank"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomNavigationView),
                                        0),
                                2),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        onView(withId(R.id.coin_amount_bank)).perform(typeText("11"));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.submit_desposit), withText("Deposit"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.container),
                                        1),
                                5),
                        isDisplayed()));
        appCompatButton.perform(click());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }

        onView(withId(R.id.coin_amount_bank)).check(matches(hasErrorText(mActivityTestRule.getActivity().getString(R.string.not_enough_coins))));

    }

    @Test
    public void userDepoistsCoinsWhenTheyHaveHitLimit() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(TestUtils.TEST_EMAIL, TestUtils.TEST_PASSWORD)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        DocumentReference documentReference = database.collection("users")
                                .document(authResult.getUser().getUid());
                        HashMap<String, Object> add_coins = new HashMap<String, Object>() {{
                           put("coins_today",25);
                        }};
                        documentReference.set(add_coins, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                ViewInteraction editText = onView(
                                        allOf(withId(R.id.email_edit_text),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.cardView),
                                                                0),
                                                        5),
                                                isDisplayed()));
                                editText.perform(replaceText("test@test.com"), closeSoftKeyboard());

                                // Added a sleep statement to match the app's execution delay.
                                // The recommended way to handle such scenarios is to use Espresso idling resources:
                                // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                ViewInteraction editText2 = onView(
                                        allOf(withId(R.id.password_edit_text),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.cardView),
                                                                0),
                                                        4),
                                                isDisplayed()));
                                editText2.perform(replaceText("test_password"), closeSoftKeyboard());

                                // Added a sleep statement to match the app's execution delay.
                                // The recommended way to handle such scenarios is to use Espresso idling resources:
                                // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                ViewInteraction button = onView(
                                        allOf(withId(R.id.signin_button), withText("Sign In"),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.cardView),
                                                                0),
                                                        2),
                                                isDisplayed()));
                                button.perform(click());

                                // Added a sleep statement to match the app's execution delay.
                                // The recommended way to handle such scenarios is to use Espresso idling resources:
                                // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                ViewInteraction bottomNavigationItemView = onView(
                                        allOf(withId(R.id.action_bank), withContentDescription("Bank"),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.bottomNavigationView),
                                                                0),
                                                        2),
                                                isDisplayed()));
                                bottomNavigationItemView.perform(click());

                                onView(withId(R.id.coin_amount_bank)).perform(typeText("1"));

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.fillInStackTrace();
                                }

                                ViewInteraction appCompatButton = onView(
                                        allOf(withId(R.id.submit_desposit), withText("Deposit"),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.container),
                                                                1),
                                                        5),
                                                isDisplayed()));
                                appCompatButton.perform(click());

                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.fillInStackTrace();
                                }

                                onView(withId(R.id.coin_amount_bank)).check(matches(hasErrorText(mActivityTestRule.getActivity().getString(R.string.too_many_coins_today))));
                            }
                        });
                    }
                });
    }
    @Test
    public void userDespositsMoreCoinsThanTheyCanToday() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(TestUtils.TEST_EMAIL, TestUtils.TEST_PASSWORD)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        DocumentReference documentReference = database.collection("users")
                                .document(authResult.getUser().getUid());
                        HashMap<String, Object> add_coins = new HashMap<String, Object>() {{
                            put("coins_today",24);
                        }};
                        documentReference.set(add_coins, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                ViewInteraction editText = onView(
                                        allOf(withId(R.id.email_edit_text),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.cardView),
                                                                0),
                                                        5),
                                                isDisplayed()));
                                editText.perform(replaceText("test@test.com"), closeSoftKeyboard());

                                // Added a sleep statement to match the app's execution delay.
                                // The recommended way to handle such scenarios is to use Espresso idling resources:
                                // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                ViewInteraction editText2 = onView(
                                        allOf(withId(R.id.password_edit_text),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.cardView),
                                                                0),
                                                        4),
                                                isDisplayed()));
                                editText2.perform(replaceText("test_password"), closeSoftKeyboard());

                                // Added a sleep statement to match the app's execution delay.
                                // The recommended way to handle such scenarios is to use Espresso idling resources:
                                // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                ViewInteraction button = onView(
                                        allOf(withId(R.id.signin_button), withText("Sign In"),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.cardView),
                                                                0),
                                                        2),
                                                isDisplayed()));
                                button.perform(click());

                                // Added a sleep statement to match the app's execution delay.
                                // The recommended way to handle such scenarios is to use Espresso idling resources:
                                // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                ViewInteraction bottomNavigationItemView = onView(
                                        allOf(withId(R.id.action_bank), withContentDescription("Bank"),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.bottomNavigationView),
                                                                0),
                                                        2),
                                                isDisplayed()));
                                bottomNavigationItemView.perform(click());

                                onView(withId(R.id.coin_amount_bank)).perform(typeText("2"));

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.fillInStackTrace();
                                }

                                ViewInteraction appCompatButton = onView(
                                        allOf(withId(R.id.submit_desposit), withText("Deposit"),
                                                childAtPosition(
                                                        childAtPosition(
                                                                withId(R.id.container),
                                                                1),
                                                        5),
                                                isDisplayed()));
                                appCompatButton.perform(click());

                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.fillInStackTrace();
                                }

                                String to_check =mActivityTestRule.getActivity().getString(R.string.too_many_coins_today);
                                onView(withId(R.id.coin_amount_bank)).check(matches(hasErrorText(String.format(to_check, "2"))));
                            }
                        });
                    }
                });
    }

    Matcher<View> hasValueEqualTo(final String content) {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Has EditText/TextView the value:  " + content);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView) && !(view instanceof EditText)) {
                    return false;
                }
                if (view != null) {
                    String text;
                    if (view instanceof TextView) {
                        text = ((TextView) view).getText().toString();
                    } else {
                        text = ((EditText) view).getText().toString();
                    }

                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }
        };
    }
    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
