package com.example.android.songle;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

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
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class GiveUpTest {

    @Rule
    public ActivityTestRule<SplashScreen> mActivityTestRule = new ActivityTestRule<>(SplashScreen.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    /**
     * Clears everything in the SharedPreferences
     */

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
    public void giveUpTest() {
        //Click the start button
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.startButton), isDisplayed()));
        appCompatButton.perform(click());

        //Click the new song button
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.new_song_button), withText("New Song")));
        appCompatButton2.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Go back to the main menu
        pressBack();

        //Confirm going back by clicking the exit button in the alert dialog
        ViewInteraction button = onView(
                allOf(withId(android.R.id.button1), withText("Exit")));
        button.perform(scrollTo(), click());

        //Make sure the incompleteSong is there
        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.incomplete_song_layout),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.songs_list),
                                        0),
                                0),
                        isDisplayed()));
        linearLayout.check(matches(isDisplayed()));

        //GiveUp button clicked
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.list_item_give_up),
                        withParent(withId(R.id.incomplete_song_layout)),
                        isDisplayed()));
        appCompatButton3.perform(click());

        //Confirm give up by clicking the give up button in the alert dialog
        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("Give Up")));
        appCompatButton4.perform(scrollTo(), click());


        //Make sure the incompleteSong is no longer there
        ViewInteraction linearLayout2 = onView(
                allOf(withId(R.id.incomplete_song_layout),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.songs_list),
                                        0),
                                0),
                        isDisplayed()));
        linearLayout2.check(doesNotExist());


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
