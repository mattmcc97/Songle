package com.example.android.songle;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class TopBarFragment extends PreferenceFragment implements View.OnClickListener {


    Dialog dialog;
    TextView levelTextView;

    private ProgressBar levelPb = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflate the layout for this fragment.
        View myLayout = inflater.inflate(R.layout.fragment_top_bar, container, false);

        //Add an OnClick listener to the help/instructions button.
        Button helpButton = myLayout.findViewById(R.id.help_button);
        helpButton.setOnClickListener(this);

        levelPb = myLayout.findViewById(R.id.level_pb);
        levelTextView = myLayout.findViewById(R.id.level_tv);

        updateLevel();

        return myLayout;
    }

    /*
            This method is responsible for getting the latest number of points the user has and
            calculating their level and their progress within that level.
     */
    public void updateLevel() {

        //The current score is retrieved from SharedPreferences.
        SharedPreferences scoreAndLevel = getActivity().getSharedPreferences("score", Context.MODE_PRIVATE);
        int currentScore = scoreAndLevel.getInt("score", 0);

        //Every level has 1000 points
        double level = currentScore / 1000.0;
        int integerLevel = (int) level;

        /*
                The user's level could be 4.325, therefore the integer level would be 4 so the
                progress in that level would be calculated as follows:

                4.325 - 4 = 0.325 , 0.325*1000 = 325.
         */
        int levelProgress = (int) ((level - integerLevel) * 1000);


        levelPb.setMax(1000);
        levelPb.setProgress(levelProgress);

        //Add 1 to the user's level so that the levels start at 1 and not 0.
        levelTextView.setText("Level " + (integerLevel + 1));

        SharedPreferences.Editor editor = scoreAndLevel.edit();
        editor.putInt("level", (integerLevel + 1));
        editor.apply();

    }


    /*
            The instructions are shown on the screen with the information/help button is clicked
            in the top left of the screen.
     */
    @Override
    public void onClick(View v) {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.help_dialog);
        dialog.show();

        //The OK button will close the dialog.
        Button okBtn = dialog.findViewById(R.id.help_ok_button);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

}
