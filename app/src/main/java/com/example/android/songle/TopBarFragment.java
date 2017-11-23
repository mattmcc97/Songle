package com.example.android.songle;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.SharedPreferencesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class TopBarFragment extends PreferenceFragment implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {


    Dialog dialog;
    TextView levelTextView;

    private ProgressBar levelPb = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View myLayout = inflater.inflate(R.layout.fragment_top_bar, container, false);

        // add click listener to the button
        Button helpButton = (Button) myLayout.findViewById(R.id.help_button);
        helpButton.setOnClickListener(this);

        SharedPreferences sharedPrefs = getActivity().getSharedPreferences("score", Context.MODE_PRIVATE);
        int currentScore = sharedPrefs.getInt("score", 0);

        double level = currentScore/1000.0;
        int integerLevel = (int) level;

        //level could be 4.325 integer level would be 4 so levelProgress would be
        //4.325 - 4 = 0.325 , 0.325*1000 = 325.
        int levelProgress = (int) ((level - integerLevel)*1000);

        levelPb = (ProgressBar) myLayout.findViewById(R.id.level_pb);
        levelPb.setMax(1000);
        levelPb.setProgress(levelProgress);

        levelTextView = (TextView) myLayout.findViewById(R.id.level_tv);
        //add 1 to level so user starts at level 1 not level 0
        levelTextView.setText("Level " + (integerLevel + 1));


        return myLayout;
    }


    @Override
    public void onClick(View v) {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.help_dialog);
        dialog.setTitle("Help");
        dialog.show();

        Button okBtn = (Button) dialog.findViewById(R.id.help_ok_button);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(getActivity());

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i("beep", "onSharedPreferenceChanged: nochance");
        if (key.equals("points"))
        {
            /* Set summary to be the user-description for the selected value
            Preference exercisesPref = findPreference(key);
            exercisesPref.setSummary(sharedPreferences.getString(key, ""));*/
        }
    }
}
