package com.example.android.songle;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class TopBarFragment extends Fragment implements  View.OnClickListener{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View myLayout = inflater.inflate(R.layout.fragment_top_bar, container, false);

        // add click listener to the button
        Button helpButton = (Button) myLayout.findViewById(R.id.help_button);
        helpButton.setOnClickListener(this);

        return myLayout;
    }

    @Override
    public void onClick(View v) {
        
    }


}
