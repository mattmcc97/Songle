package com.example.android.songle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void startGame(View view) {
        //When the new song button is clicked, open the MapsActivity
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }
}
