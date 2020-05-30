package com.example.MCLShuttleTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountEditActivity extends AppCompatActivity {

    Button btnSave, btnCancel;
    EditText txtDriverId, txtFirstName, txtLastName, txtPassword, txtConfirmPass;
    NumberPicker numCapacity;

    String driverId;

    DatabaseReference driver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);

        driverId = getIntent().getStringExtra("driverId");

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        txtDriverId = findViewById(R.id.txtDriverId);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPass = findViewById(R.id.txtConfirmPass);
        numCapacity = findViewById(R.id.numCapacity);

        numCapacity.setMaxValue(24);
        numCapacity.setMinValue(4);

        txtDriverId.setText(driverId);

        driver = FirebaseDatabase.getInstance().getReference("Drivers/" + driverId);
        driver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtFirstName.setText(dataSnapshot.child("firstName").getValue().toString());
                txtLastName.setText(dataSnapshot.child("lastName").getValue().toString());
                numCapacity.setValue(Integer.valueOf(dataSnapshot.child("capacity").getValue().toString()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String driverId = txtDriverId.getText().toString();
                final String firstName = txtFirstName.getText().toString();
                final String lastName = txtLastName.getText().toString();
                final String password = txtPassword.getText().toString();
                final String confirmPass = txtConfirmPass.getText().toString();
                final int capacity = numCapacity.getValue();

                if (driverId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPass.isEmpty()){
                    ShowToast("Please fill out all fields");
                }
                else if (!password.equals(confirmPass)){
                    ShowToast("Passwords do not match");
                }
                else{
                    driver.child("firstName").setValue(firstName);
                    driver.child("lastName").setValue(lastName);
                    driver.child("password").setValue(password);
                    driver.child("capacity").setValue(capacity);

                    ShowToast("Account Saved successfully!");
                    finish();
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
