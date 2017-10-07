package com.example.android.songle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainMenu extends AppCompatActivity {

    private ProgressBar levelPb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        levelPb = (ProgressBar) findViewById(R.id.level_pb);
        levelPb.setMax(100);
        levelPb.setProgress(50);
    }

    public void newSong(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

}
