package com.example.android.songle;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.times;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by Matthew on 05/12/2017.
 */

public class IntentMainMenuTest {

    @Rule
    public IntentsTestRule<MainMenu> mIntentRule = new IntentsTestRule<MainMenu>(MainMenu.class);
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

    //Test that when the new song button is clicked, it moves to the maps activity
    @Test
    public void testNewSongButton(){
        testClickOnButton(R.id.new_song_button, MapsActivity.class);
    }

    //Test that when an incomplete song button is clicked, it moves to the maps activity
    //First we must insert an incomplete song into the main menu
    @Test
    public void testTheIncompleteSongButton(){

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

        //Go back to the main menu
        pressBack();

        //Confirm exit to main menu
        ViewInteraction button = onView(
                allOf(withId(android.R.id.button1), withText("Exit")));
        button.perform(scrollTo(), click());

        onView(withId(R.id.incomplete_song_layout)).perform(click());
        intended(hasComponent(MapsActivity.class.getName()), times(2));
    }

    //Test that when the statistics button is clicked, it moves to the statistics activity
    @Test
    public void testStatisticsButton(){
        testClickOnButton(R.id.statistics_button, StatisticsActivity.class);
    }

    private void testClickOnButton(int id, Class newClass){
        onView(withId(id)).perform(click());
        intended(hasComponent(newClass.getName()));
    }
}
