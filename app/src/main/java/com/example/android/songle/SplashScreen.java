package com.example.android.songle;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Move instantly from the splash screen to the main menu
        Intent mainMenuIntent = new Intent(SplashScreen.this, MainMenu.class);
        startActivity(mainMenuIntent);
        finish();
    }
}
