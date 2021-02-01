package com.example.MCLShuttleTracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

public class DestinationEditActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap gMap;

    Button btnDelete, btnCancel, btnSave;
    EditText txtName;
    TextView txtTitle;

    String id, name, address;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_edit);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);
        txtName = findViewById(R.id.txtDestinationName);
        txtTitle = findViewById(R.id.txtTitle);

//        mode = getIntent().getStringExtra("mode");
//
//        if(mode.equals("PickUps") ){
//            txtName.setHint("Location Name");
//            txtTitle.setText("Edit Pick Up Location");
//        }

        id = getIntent().getStringExtra("destinationId");
        name = getIntent().getStringExtra("destinationName");
        address = getIntent().getStringExtra("destinationAddress");
        latitude = getIntent().getDoubleExtra("destinationLatitude",14.2439236);
        longitude = getIntent().getDoubleExtra("destinationLongitude",121.1123045);


        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.frgMap);
        mapFragment.getMapAsync(this);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = txtName.getText().toString();
                if(name.isEmpty()){
                    ShowToast("Please enter a name for this location");
                }
                else{
                    DatabaseReference destination = FirebaseDatabase.getInstance().getReference("Stations/" + id);

                    destination.child("address").setValue(address);
                    destination.child("latitude").setValue(latitude);
                    destination.child("longitude").setValue(longitude);
                    destination.child("name").setValue(name);

                    ShowToast("Location Edited Successfully");
                    finish();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DestinationEditActivity.this);
                builder.setMessage("Are you sure you want to delete this location?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseReference destination = FirebaseDatabase.getInstance().getReference("Stations");

                                destination.child(id).removeValue();

                                ShowToast("Location Deleted Successfully");
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;

        gMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude) , 16f) );

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude,longitude));
        markerOptions.title(name);

        gMap.clear();
        gMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude)));
        gMap.addMarker(markerOptions);

        txtName.setText(name);

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                latitude = latLng.latitude;
                longitude = latLng.longitude;

                Geocoder geocoder = new Geocoder(DestinationEditActivity.this);

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
