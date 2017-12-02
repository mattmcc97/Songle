package com.example.android.songle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    /*
            This activity only appears while the app is starting up. It does not cause any slowing
            of the loading process as it does not sleep in the OnCreate method.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Move instantly from the splash screen to the main menu
        Intent mainMenuIntent = new Intent(SplashScreen.this, StartActivity.class);
        startActivity(mainMenuIntent);
        finish();
    }
}
