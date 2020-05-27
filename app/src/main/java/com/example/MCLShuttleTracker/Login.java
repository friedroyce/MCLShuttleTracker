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

public class Login extends AppCompatActivity {
    Button btnLogin;
    Button btnRegister;
    EditText txtDriverId;
    EditText txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        txtDriverId = findViewById(R.id.txtDriverId);
        txtPassword = findViewById(R.id.txtPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String id = txtDriverId.getText().toString();
                final String pass = txtPassword.getText().toString();
                if (id.isEmpty() || pass.isEmpty()){
                    ShowToast("Please enter your ID and password");
                }
                else{
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Drivers");
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            if (!dataSnapshot.hasChild(id)) {
                                // The driver doesn't exist
                                ShowToast("The user does not exist");
                            }else if (dataSnapshot.child(id).child("Password").getValue().toString().equals(pass)){
                                ShowToast("User login successful");
                            }
                            else ShowToast("Incorrect credentials");
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                        }
                    });
                }
            }
        });
    }

    void ShowToast(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }
}
