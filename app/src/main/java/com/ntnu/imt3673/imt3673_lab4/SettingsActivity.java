package com.ntnu.imt3673.imt3673_lab4;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(
            android.R.id.content, new SettingsFragment()
        ).commit();
    }

}
