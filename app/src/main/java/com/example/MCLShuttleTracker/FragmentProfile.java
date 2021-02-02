package com.example.MCLShuttleTracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentProfile extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    Button btnSave, btnCancel;
    EditText txtDriverId, txtFirstName, txtLastName, txtPassword, txtConfirmPass;
    NumberPicker numCapacity;

    String driverId;

    DatabaseReference driver;

    public FragmentProfile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentProfile.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentProfile newInstance(String param1, String param2) {
        FragmentProfile fragment = new FragmentProfile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        driverId = getActivity().getIntent().getStringExtra("driverId");

        btnSave = rootView.findViewById(R.id.btnSave2);
        btnCancel = rootView.findViewById(R.id.btnCancel2);
        txtDriverId = rootView.findViewById(R.id.txtDriverId2);
        txtFirstName = rootView.findViewById(R.id.txtFirstName2);
        txtLastName = rootView.findViewById(R.id.txtLastName2);
        txtPassword = rootView.findViewById(R.id.txtPassword2);
        txtConfirmPass = rootView.findViewById(R.id.txtConfirmPass2);
        numCapacity = (NumberPicker) rootView.findViewById(R.id.numCapacity2);

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
                    //clear fields
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //clear fields
            }
        });

        return rootView;
    }

    void ShowToast(String message){ Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show(); }
}