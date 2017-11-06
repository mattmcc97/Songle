package com.example.android.songle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if no songle coins
                Snackbar.make(view, "No Songle Coins available.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                //else - are you sure you want to reveal 5 lyrics using 1 songle coin?

            }
        });*/
    }

    public void submitGuess(View view) {
        EditText songGuessEt = (EditText) (findViewById(R.id.song_guess_et));
        if (songGuessEt.getText().toString().equalsIgnoreCase(getIntent().getStringExtra("songTitle"))) {
            dialogCorrect = new Dialog(this);
            dialogCorrect.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogCorrect.setContentView(R.layout.dialog_correct_answer);
            dialogCorrect.show();

            TextView songTitleTv = (TextView) dialogCorrect.findViewById(R.id.song_title_tv);
            songTitleTv.setText(getIntent().getStringExtra("songTitle"));

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

    public void spendSongleCoin(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        title.setText("Spend Songle coin?");
        title.setPadding(10, 50, 10, 0);
        title.setTextColor(Color.DKGRAY);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(20);

        alertDialogBuilder.setCustomTitle(title);

        alertDialogBuilder.setTitle("Spend Songle coin?");
        alertDialogBuilder.setMessage("Are you sure you want to spend a Songle coin? This will" +
                " reveal 10 words from the song you are playing.");
        alertDialogBuilder.setPositiveButton("Spend",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        TextView messageText = (TextView) alertDialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        alertDialog.show();
    }

    private void backToMainMenu() {

        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);

    }
}
