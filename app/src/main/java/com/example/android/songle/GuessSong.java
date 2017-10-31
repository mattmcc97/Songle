package com.example.android.songle;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class GuessSong extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_song);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "No Songle Coins available.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void submitGuess(View view) {
        EditText songGuessEt = (EditText) (findViewById(R.id.song_guess_et));
        if (songGuessEt.getText().toString().equalsIgnoreCase(getIntent().getStringExtra("songTitle"))) {
            Toast.makeText(GuessSong.this, "Correct.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(GuessSong.this, "Wrong. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
