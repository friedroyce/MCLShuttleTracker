package com.example.MCLShuttleTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 15;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PEMISSIONS_FINE_LOCATION = 99;
    //references to ui elements
    Switch swTracking, swGPS;
    TextView txtSensors;

    DatabaseReference driver;

    //Google's API for location services
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swTracking = findViewById(R.id.swTracking);
        swGPS = findViewById(R.id.swGPS);
        txtSensors = findViewById(R.id.txtSensors);

        //get driver reference from firebase
        String driverId = getIntent().getStringExtra("driverId");
        driver = FirebaseDatabase.getInstance().getReference("Drivers/" + driverId);

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

        swTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(swTracking.isChecked()){
                    startLocationUpdates();
                    driver.child("status").setValue(true);
                }
                else{
                    stopLocationUpdates();
                    driver.child("trackingOn").setValue(false);
                }
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
        //update driver in firebase database

        final String latitude = String.valueOf(location.getLatitude());
        final String longitude = String.valueOf(location.getLongitude());
        final String accuracy = String.valueOf(location.getAccuracy());
        String altitude;
        String speed;
        String address;

        if(location.hasAltitude()){
            altitude = String.valueOf(location.getAltitude());
        }
        else altitude = "Not Available";

        if(location.hasSpeed()){
            speed = String.valueOf(location.getAltitude());
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

        driver.child("location").child("latitude").setValue(latitude);
        driver.child("location").child("longitude").setValue(longitude);
        driver.child("location").child("accuracy").setValue(accuracy);
        driver.child("location").child("altitude").setValue(altitude);
        driver.child("location").child("speed").setValue(speed);
        driver.child("location").child("address").setValue(address);

    }
}
