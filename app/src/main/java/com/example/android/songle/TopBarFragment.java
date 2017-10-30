package com.example.android.songle;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;


public class TopBarFragment extends Fragment implements  View.OnClickListener{


    Dialog dialog;

    private ProgressBar levelPb = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View myLayout = inflater.inflate(R.layout.fragment_top_bar, container, false);

        // add click listener to the button
        Button helpButton = (Button) myLayout.findViewById(R.id.help_button);
        helpButton.setOnClickListener(this);

        levelPb = (ProgressBar) myLayout.findViewById(R.id.level_pb);
        levelPb.setMax(100);
        levelPb.setProgress(50);

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


}
