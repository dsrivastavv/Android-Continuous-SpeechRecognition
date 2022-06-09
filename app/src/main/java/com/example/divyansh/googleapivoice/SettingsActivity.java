package com.example.divyansh.googleapivoice;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {


    private TextView heading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        heading = findViewById(R.id.settingshead);
        // set up home button
        ImageButton homeButton;
        homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // inflate settings fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.list_container, new SettingsFragment())
                .commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(this);

        //preferenceFrag.setTypeface(typeface);

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}


