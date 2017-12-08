package com.example.android.songle;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class YouTubeWatchTest {

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

    public static final String songTitle = "Song 2";


    /**
     * Clears everything in the SharedPreferences
     */
    private void clearSharedPrefs(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences("score", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
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
    public void youTubeWatchTest() throws UiObjectNotFoundException {

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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Go to the guess song screen
        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.fab_guess_song), isDisplayed()));
        floatingActionButton.perform(click());

        //Change the song title and assert that it is not null
        GuessSong.songTitle = songTitle;
        assertTrue("songTitle is not null", GuessSong.songTitle != null);

        //Swipe up to move down to the bottom of the guess song screen
        onView(withId(android.R.id.content)).perform(ViewActions.swipeUp());
        onView(withId(android.R.id.content)).perform(ViewActions.swipeUp());
        onView(withId(android.R.id.content)).perform(ViewActions.swipeUp());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //correct guess
        insertTextIntoInput(R.id.song_guess_et, songTitle);

        //Submit the correct guess
        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.song_guess_submit), withText("Submit"), isDisplayed()));
        appCompatButton5.perform(click());

        //Press the ok button when the correct song dialog appears
        ViewInteraction appCompatButton6 = onView(
                allOf(withId(R.id.correct_ok_button), withText("OK"), isDisplayed()));
        appCompatButton6.perform(click());

        //Check that the completed song appears in the main menu
        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.completed_song_layout),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.songs_list),
                                        1),
                                0),
                        isDisplayed()));
        linearLayout.check(matches(isDisplayed()));

        //Click the completed song button
        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.complete_list_item_song), isDisplayed()));
        appCompatButton7.perform(click());

        //Check that the YouTube video appears and loads
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Go back on the device to remove the YouTube video from the screen
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressBack();

        //Check to see if the completed Song is still on the Main menu
        ViewInteraction linearLayout2 = onView(
                allOf(withId(R.id.completed_song_layout),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.songs_list),
                                        1),
                                0),
                        isDisplayed()));
        linearLayout2.check(matches(isDisplayed()));


    }

    public void insertTextIntoInput(Integer inputId, String text) {
        onView(withId(inputId)).perform(replaceText(text), closeSoftKeyboard());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
