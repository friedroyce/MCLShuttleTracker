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

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.datatransport.runtime.Destination;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 15;
    public static final int FASTEST_UPDATE_INTERVAL = 5;
    private static final int PEMISSIONS_FINE_LOCATION = 99;
    //references to ui elements
    Switch swGPS, swTracking;
    TextView txtSensors, txtTracking, txtReservations, txtETA, txtStatus;
    Spinner spnDestination;
    Button btnEditProfile, btnDestinations, btnSchedules, btnStatus;

    DatabaseReference refRoot;
    DatabaseReference refDriver;
    DatabaseReference refLocation;
    DatabaseReference refDestinations;
    DatabaseReference refReservations;
    FirebaseListOptions<DestinationModel> options;
    DestinationModel[] destinationArr;

    String driverId;
    String status;
    int destinationSelectIndex = 0, reservation = 0, capacity = 0;

    //Google's API for location services
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swGPS = findViewById(R.id.swGPS);
        swTracking = findViewById(R.id.swTracking);
        txtSensors = findViewById(R.id.txtSensors);
        txtStatus = findViewById(R.id.txtStatus);
        txtETA = findViewById(R.id.txtETA);
        txtTracking = findViewById(R.id.txtTracking);
        txtReservations = findViewById(R.id.txtReservations);
        spnDestination = findViewById(R.id.spnDestination);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDestinations = findViewById(R.id.btnDestinations);
        btnSchedules = findViewById(R.id.btnSchedules);
        btnStatus = findViewById(R.id.btnStatus);

        //get driver reference from firebase
        driverId = getIntent().getStringExtra("driverId");
        refRoot = FirebaseDatabase.getInstance().getReference();
        refDriver = refRoot.child("Drivers/" + driverId);
        refLocation = refRoot.child("Tracking/" + driverId);
        refReservations = refRoot.child("Reservations/" + driverId);
        refDestinations = refRoot.child("Destinations");

        status = "Waiting";

        refDriver.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                capacity = Integer.valueOf(dataSnapshot.child("capacity").getValue().toString());
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

        refReservations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservation = (int) dataSnapshot.getChildrenCount();
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        refDestinations.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count  = String.valueOf(dataSnapshot.getChildrenCount());
                destinationArr = new DestinationModel[ Integer.valueOf(count)];

                options = new FirebaseListOptions.Builder<DestinationModel>().setQuery(refDestinations, DestinationModel.class).setLayout(R.layout.list_item_destination).build();

                FirebaseListAdapter<DestinationModel> firebaseListAdapter = new FirebaseListAdapter<DestinationModel>(options) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull DestinationModel model, int position) {

                        DatabaseReference itemRef = getRef(position);

                        TextView txtName = v.findViewById(R.id.txtDestinationName);
                        TextView txtAddress = v.findViewById(R.id.txtDestinationAddress);

                        txtName.setText(model.getName());
                        txtAddress.setText(String.valueOf(model.getAddress()));
                        destinationArr[position] = new DestinationModel();
                        destinationArr[position].setId(itemRef.getKey());
                        destinationArr[position].setName(model.getName());
                        destinationArr[position].setLatitude(model.getLatitude());
                        destinationArr[position].setLongitude(model.getLongitude());
                        destinationArr[position].setAddress(model.getAddress());

                    }
                };

                firebaseListAdapter.startListening();
                spnDestination.setAdapter(firebaseListAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

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
                    refLocation.child("status").setValue(status);
                    refLocation.child("destination").setValue(destinationArr[destinationSelectIndex].getId());
                    startLocationUpdates();
                }
                else {
                    stopLocationUpdates();
                    DatabaseReference tracking = FirebaseDatabase.getInstance().getReference("Tracking");
                    tracking.child(driverId).removeValue();
                }
                updateUI();
            }
        });

        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status.equals("Waiting")){
                    status = "In Transit";
                    refLocation.child("status").setValue(status);
                }
                else{
                    status = "Waiting";
                    refLocation.child("status").setValue(status);
                    refReservations.removeValue();
                }
                updateUI();
            }
        });

        spnDestination.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                destinationSelectIndex = position;
                switch (status){
                    case "In Transit":
                    case "Waiting":
                        refLocation.child("destination").setValue(destinationArr[destinationSelectIndex].getId());
                        break;
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

    private void updateUI(){

        txtReservations.setText("Reserved Students (" + reservation + "/" + capacity + ")");

        if (status.equals("Waiting")){
            btnStatus.setText("Depart");
            swTracking.setEnabled(true);
            spnDestination.setEnabled(true);
            txtStatus.setText(status);
        }
        else{
            btnStatus.setText("Unload Students");
            swTracking.setEnabled(false);
            spnDestination.setEnabled(false);
            txtStatus.setText(status);
        }

        if(swTracking.isChecked()){
            txtTracking.setText("Tracking Enabled");
            btnStatus.setEnabled(true);
            btnEditProfile.setEnabled(false);
            btnSchedules.setEnabled(false);
            btnDestinations.setEnabled(false);
            txtStatus.setText(status);
        }
        else {
            txtTracking.setText("Tracking Disabled");
            btnStatus.setEnabled(false);
            btnEditProfile.setEnabled(true);
            btnSchedules.setEnabled(true);
            btnDestinations.setEnabled(true);
            txtStatus.setText("Tracking Disabled");
        }


    }

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

        if(swTracking.isChecked()){
            refLocation.child("latitude").setValue(latitude);
            refLocation.child("longitude").setValue(longitude);
            refLocation.child("accuracy").setValue(accuracy);
            refLocation.child("altitude").setValue(altitude);
            refLocation.child("speed").setValue(speed);
            refLocation.child("address").setValue(address);
            refLocation.child("destination").setValue(destinationArr[destinationSelectIndex].getId());
        }

    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
