package com.example.MCLShuttleTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 15;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PEMISSIONS_FINE_LOCATION = 99;
    //references to ui elements
    Switch swGPS;
    TextView txtSensors, txtReserved, txtCapacity, txtETA;
    Spinner spnStatus, spnDestination;
    Button btnEditProfile, btnDestinations, btnSchedules;

    DatabaseReference refDriver;
    DatabaseReference refLocation;
    String driverId;
    String status;

    //Google's API for location services
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swGPS = findViewById(R.id.swGPS);
        txtSensors = findViewById(R.id.txtSensors);
        txtReserved = findViewById(R.id.txtReserved);
        txtCapacity = findViewById(R.id.txtCapacity);
        txtETA = findViewById(R.id.txtETA);
        spnStatus = findViewById(R.id.spnStatus);
        spnDestination = findViewById(R.id.spnDestination);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDestinations = findViewById(R.id.btnDestinations);
        btnSchedules = findViewById(R.id.btnSchedules);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.status));
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStatus.setAdapter(statusAdapter);
        spnStatus.setSelection(2);

        //get driver reference from firebase
        driverId = getIntent().getStringExtra("driverId");
        refDriver = FirebaseDatabase.getInstance().getReference("Drivers/" + driverId);

        refLocation = FirebaseDatabase.getInstance().getReference("Tracking/" + driverId);

        status = spnStatus.getSelectedItem().toString();

        refDriver.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtCapacity.setText(dataSnapshot.child("capacity").getValue().toString());
                txtReserved.setText(String.valueOf(dataSnapshot.child("reservations").getChildrenCount()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

        txtCapacity.setText(refDriver.child("capacity").getKey());

        //initialize location request
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //this is triggered whenever the location update interval is met
        locationCallBack = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateDriverLocation(locationResult.getLastLocation());
            }
        };

        swGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(swGPS.isChecked()){
                    //most accurate
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    txtSensors.setText("Using GPS sensors");
                }
                else{
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    txtSensors.setText("Using cellular towers and WiFi sensors");
                }
            }
        });

        spnStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                status = spnStatus.getSelectedItem().toString();

                switch (status){
                    case "In Transit":
                    case "Waiting":
                        refLocation.child("status").setValue(status);
                        startLocationUpdates();
                        break;
                    case "Tracking Disabled":
                        stopLocationUpdates();
                        DatabaseReference destination = FirebaseDatabase.getInstance().getReference("Tracking");
                        destination.child(driverId).removeValue();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });

        btnDestinations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DestinationsActivity.class);
                startActivity(intent);
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AccountEditActivity.class);
                intent.putExtra("driverId", driverId);
                startActivity(intent);
            }
        });

        updateGPS();
    }//end of on create

    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PEMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this, "This app requires permissions to be granted to work properly", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void updateGPS(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateDriverLocation(location);
                }
            });
        }
        else{
            //permission not granted yet

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PEMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateDriverLocation(Location location){
        //update refDriver in firebase database

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        final float accuracy = location.getAccuracy();
        String altitude;
        String speed;
        String address;

        if(location.hasAltitude()){
            altitude = String.valueOf(location.getAltitude());
        }
        else altitude = "Not Available";

        if(location.hasSpeed()){
            speed = String.valueOf(location.getSpeed());
        }
        else speed = "Not Available";

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
        }
        catch (Exception e){
            address = "Unable to get street address";
        }

        switch (status){
            case "In Transit":
            case "Waiting":
                refLocation.child("latitude").setValue(latitude);
                refLocation.child("longitude").setValue(longitude);
                refLocation.child("accuracy").setValue(accuracy);
                refLocation.child("altitude").setValue(altitude);
                refLocation.child("speed").setValue(speed);
                refLocation.child("address").setValue(address);
                break;
        }

    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
