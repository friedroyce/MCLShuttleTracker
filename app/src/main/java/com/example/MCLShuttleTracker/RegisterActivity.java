package com.example.MCLShuttleTracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    Button btnRegister;
    Button btnCancel;
    EditText txtDriverId;
    EditText txtFirstName;
    EditText txtLastName;
    EditText txtPassword;
    EditText txtConfirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegister = findViewById(R.id.btnRegister);
        btnCancel = findViewById(R.id.btnCancel);
        txtDriverId = findViewById(R.id.txtDriverId);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtLastName = findViewById(R.id.txtLastName);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPass = findViewById(R.id.txtConfirmPass);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String driverId = txtDriverId.getText().toString();
                final String firstName = txtFirstName.getText().toString();
                final String lastName = txtLastName.getText().toString();
                final String password = txtPassword.getText().toString();
                final String confirmPass = txtConfirmPass.getText().toString();

                if (driverId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPass.isEmpty()){
                    ShowToast("Please fill out all fields");
                }
                else if (!password.equals(confirmPass)){
                    ShowToast("Passwords do not match");
                }
                else{
                    final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Drivers");
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(driverId)) {
                                ShowToast("A driver with that ID already exists");
                            }
                            else {
                                DatabaseReference driver = rootRef.child(driverId);
                                driver.child("firstName").setValue(firstName);
                                driver.child("lastName").setValue(lastName);
                                driver.child("password").setValue(password);

                                ShowToast("Registration successful!");
                                finish();
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

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
