package com.example.android.songle.SongleCoinTests;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.example.android.songle.R;
import com.example.android.songle.SplashScreen;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class SpendSongleCoin {

    @Rule
    public ActivityTestRule<SplashScreen> mActivityTestRule = new ActivityTestRule<SplashScreen>(SplashScreen.class){
        @Override
        protected void beforeActivityLaunched() {
            clearSharedPrefs(InstrumentationRegistry.getTargetContext());
            super.beforeActivityLaunched();
        }
    };
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    /**
     * Clears everything in the SharedPreferences
     */
    private void clearSharedPrefs(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences("score", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
        SharedPreferences prefs2 =
                context.getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor2 = prefs2.edit();
        editor2.clear();
        editor2.commit();
    }

    @Before
    public void setUp() {
        File[] files = InstrumentationRegistry.getTargetContext().getFilesDir().listFiles();
        if(files != null){
            for(File file : files) {
                file.delete();
            }
        }
    }

    @Test
    public void spendSongleCoin() {
        //Click the start button
        ViewInteraction appCompatButton = onView(
                allOf(ViewMatchers.withId(R.id.startButton), isDisplayed()));
        appCompatButton.perform(click());

        //CLick the new song button
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.new_song_button), withText("New Song")));
        appCompatButton2.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Add 1 songle coin for the user for testing purposes
        SharedPreferences songleCoins = InstrumentationRegistry.getTargetContext().getSharedPreferences("songleCoins", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorSongleCoins = songleCoins.edit();
        editorSongleCoins.putInt("currentNumberOfCoins", 1);
        editorSongleCoins.apply();

        //Go the guess song screen
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab_guess_song), isDisplayed()));
        floatingActionButton.perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Check to see if the textview displays one songle coin available
        ViewInteraction textView = onView(
                allOf(withId(R.id.number_of_coins_tv), withText("You have 1 Songle coin available."),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.scroll_view_guess_song),
                                        0),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("You have 1 Songle coin available.")));


        //Click the songle coin floating action button to spend a songle coin
        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.fab_spend_songle_coin), isDisplayed()));
        floatingActionButton2.perform(click());

        //Click the spend button in the alert dialog
        ViewInteraction appCompatButton3 = onView(
                allOf(withText("Spend")));
        appCompatButton3.perform(scrollTo(), click());

        //Check that a songle coin has been used and the textview now says that there are no songle
        //coins available
        ViewInteraction textView2 = onView(
                allOf(withId(R.id.number_of_coins_tv), withText("You have 0 Songle coins available."),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.scroll_view_guess_song),
                                        0),
                                0),
                        isDisplayed()));
        textView2.check(matches(withText("You have 0 Songle coins available.")));

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
