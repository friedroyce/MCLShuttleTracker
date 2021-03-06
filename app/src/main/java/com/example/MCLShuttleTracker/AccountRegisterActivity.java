package com.example.MCLShuttleTracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountRegisterActivity extends AppCompatActivity {

    //references to ui elements
    Button btnRegister;
    EditText txtDriverId, txtFirstName, txtLastName, txtPassword, txtConfirmPass;
    NumberPicker numCapacity;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_register);

        btnRegister = findViewById(R.id.btnRegister);
        txtDriverId = findViewById(R.id.txtDriverId);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPass = findViewById(R.id.txtConfirmPass);
        numCapacity = findViewById(R.id.numCapacity);
        btnBack = findViewById(R.id.btnBack);


        numCapacity.setMaxValue(24);
        numCapacity.setMinValue(4);
        numCapacity.setValue(13);

        btnRegister.setOnClickListener(new View.OnClickListener() {
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
                    final DatabaseReference drivers = FirebaseDatabase.getInstance().getReference("Drivers");
                    drivers.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(driverId)) {
                                ShowToast("A driver with that ID already exists");
                            }
                            else {
                                DatabaseReference driver = drivers.child(driverId);
                                driver.child("firstName").setValue(firstName);
                                driver.child("lastName").setValue(lastName);
                                driver.child("password").setValue(password);
                                driver.child("capacity").setValue(capacity);

                                ShowToast("Registration successful!");
                                startActivity(new Intent(AccountRegisterActivity.this, AccountLoginActivity.class));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            ShowToast("Failed to read database");
                        }
                    });
                }

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountRegisterActivity.this, AccountLoginActivity.class));
            }
        });


    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
