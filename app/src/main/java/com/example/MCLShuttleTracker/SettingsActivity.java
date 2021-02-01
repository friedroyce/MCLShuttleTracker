package com.example.MCLShuttleTracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    Button btnEditProfile, btnDestinations, btnTransits, btnSchedules;

    String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDestinations = findViewById(R.id.btnDestinations);
        btnSchedules = findViewById(R.id.btnSchedules);
        btnTransits = findViewById(R.id.btnTransits);

        driverId = getIntent().getStringExtra("driverId");

        btnDestinations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, DestinationsActivity.class);
                startActivity(intent);
            }
        });

        btnTransits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, DestinationsActivity.class);
                intent.putExtra("driverId", driverId);
                startActivity(intent);
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, AccountEditActivity.class);
                intent.putExtra("driverId", driverId);
                startActivity(intent);
            }
        });

        btnSchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, SchedulesActivity.class);
                startActivity(intent);
            }
        });

    }
}
