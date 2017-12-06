package com.example.android.songle;


import android.content.Context;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Root;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ThreeIncompleteSongsTest {

    @Rule
    public ActivityTestRule<SplashScreen> mActivityTestRule = new ActivityTestRule<>(SplashScreen.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void setUp() {
        File[] files = InstrumentationRegistry.getTargetContext().getFilesDir().listFiles();
        if(files != null){
            for(File file : files) {
                file.delete();
            }
        }
    }

    @After
    public void tearDown() {
        File[] files = InstrumentationRegistry.getTargetContext().getFilesDir().listFiles();
        if(files != null){
            for(File file : files) {
                file.delete();
            }
        }
    }


    @Test
    public void threeIncompleteSongsTest() {

        //Press start button
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.startButton), isDisplayed()));
        appCompatButton.perform(click());

        //Press new song button
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

        //Confirm exit to main menu
        ViewInteraction button = onView(
                allOf(withId(android.R.id.button1), withText("Exit")));
        button.perform(scrollTo(), click());

        //Create 2 new incomplete songs with the same method

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.new_song_button), withText("New Song")));
        appCompatButton3.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pressBack();

        ViewInteraction button2 = onView(
                allOf(withId(android.R.id.button1), withText("Exit")));
        button2.perform(scrollTo(), click());

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.new_song_button), withText("New Song")));
        appCompatButton4.perform(scrollTo(), click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pressBack();

        ViewInteraction button3 = onView(
                allOf(withId(android.R.id.button1), withText("Exit")));
        button3.perform(scrollTo(), click());

        //Try the new song button again when there is 3 incomplete songs
        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.new_song_button), withText("New Song")));
        appCompatButton5.perform(scrollTo(), click());

        //Check if the correct Toast appears using the ToastMatcher class
        onView(withText("The maximum number of songs you can " +
                "play, at once, is 3. Please give up on one of the " +
                "incomplete songs, if you want to guess a " +
                "new song.")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));

    }



}
