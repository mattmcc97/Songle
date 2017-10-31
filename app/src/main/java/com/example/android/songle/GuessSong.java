package com.example.android.songle;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GuessSong extends AppCompatActivity {


    Dialog dialogCorrect;
    Dialog dialogWrong;

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
                //if no songle coins
                Snackbar.make(view, "No Songle Coins available.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //else - are you sure you want to reveal 5 lyrics using 1 songle coin?
            }
        });
    }

    public void submitGuess(View view) {
        EditText songGuessEt = (EditText) (findViewById(R.id.song_guess_et));
        if (songGuessEt.getText().toString().equalsIgnoreCase(getIntent().getStringExtra("songTitle"))) {
            dialogCorrect = new Dialog(this);
            dialogCorrect.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogCorrect.setContentView(R.layout.dialog_correct_answer);
            dialogCorrect.show();

            Button okBtn = (Button) dialogCorrect.findViewById(R.id.correct_ok_button);

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogCorrect.dismiss();
                    backToMainMenu();
                }
            });

        } else {
            dialogWrong = new Dialog(this);
            dialogWrong.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogWrong.setContentView(R.layout.dialog_wrong_answer);
            dialogWrong.show();

            Button okBtn = (Button) dialogWrong.findViewById(R.id.wrong_ok_button);

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogWrong.dismiss();
                }
            });

        }
    }

    private void backToMainMenu() {

        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);

    }
}
