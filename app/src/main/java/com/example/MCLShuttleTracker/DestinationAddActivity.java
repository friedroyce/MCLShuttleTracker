package com.example.MCLShuttleTracker;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DestinationAddActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;

    Button btnAdd, btnCancel;
    EditText txtName;


    String name, address;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_add);

        btnAdd = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnCancel);
        txtName = findViewById(R.id.txtDestinationName);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.frgMap);
        mapFragment.getMapAsync(this);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = txtName.getText().toString();
                if(name.isEmpty()){
                    ShowToast("Please enter a name for this location");
                }
                else{
                    final DatabaseReference destinations = FirebaseDatabase.getInstance().getReference("Destinations");
                    destinations.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DatabaseReference destination = destinations.push();
                            destination.child("address").setValue(address);
                            destination.child("latitude").setValue(latitude);
                            destination.child("longitude").setValue(longitude);
                            destination.child("name").setValue(name);

                            ShowToast("Destination Added Successfully");
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            ShowToast("Failed to read database");
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        gMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(14.2439236,121.1123045) , 16f) );

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                btnAdd.setEnabled(true);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                latitude = latLng.latitude;
                longitude = latLng.longitude;

                Geocoder geocoder = new Geocoder(DestinationAddActivity.this);

                try{
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    address = addresses.get(0).getAddressLine(0);
                    markerOptions.title(address);
                }
                catch (Exception e){
                    address = "Unable to get street address";
                    markerOptions.title(address);
                }

                gMap.clear();
                gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                gMap.addMarker(markerOptions);
            }
        });
    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
