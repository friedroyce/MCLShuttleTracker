package com.example.MCLShuttleTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DestinationsActivity extends AppCompatActivity {

    Button btnAddDestination;
    RecyclerView recDestinations;
    ListView lstDestinations;


    ArrayList<DestinationModel> destinations = new ArrayList<>();

    DatabaseReference refDestinations;

    FirebaseListOptions<DestinationModel> options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destinations);

        btnAddDestination = findViewById(R.id.btnAddDestination);
        recDestinations = findViewById(R.id.recDestinations);
        lstDestinations = findViewById(R.id.lstDestinations);

        recDestinations.setHasFixedSize(true);
        recDestinations.setLayoutManager(new LinearLayoutManager(this));


        refDestinations = FirebaseDatabase.getInstance().getReference("Destinations");

        options = new FirebaseListOptions.Builder<DestinationModel>().setQuery(refDestinations, DestinationModel.class).setLayout(R.layout.list_item_destination).build();

        FirebaseListAdapter<DestinationModel> firebaseListAdapter = new FirebaseListAdapter<DestinationModel>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull DestinationModel model, int position) {

                DatabaseReference itemRef = getRef(position);

                TextView txtName = v.findViewById(R.id.txtDestinationName);
                TextView txtAddress = v.findViewById(R.id.txtDestinationAddress);

                txtName.setText(model.getName());
                txtAddress.setText(String.valueOf(model.getAddress()));

                destinations.add(new DestinationModel(itemRef.getKey(), model.getName(), model.getLatitude(), model.getLongitude(), model.getAddress()));
            }
        };

        firebaseListAdapter.startListening();
        lstDestinations.setAdapter(firebaseListAdapter);

        lstDestinations.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                Intent intent = new Intent(DestinationsActivity.this, DestinationEditActivity.class);
                intent.putExtra("destinationId", destinations.get(i).getId());
                intent.putExtra("destinationName", destinations.get(i).getName());
                intent.putExtra("destinationAddress", destinations.get(i).getAddress());
                intent.putExtra("destinationLatitude", destinations.get(i).getLatitude());
                intent.putExtra("destinationLongitude", destinations.get(i).getLongitude());
                startActivity(intent);
            }
        });

        btnAddDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DestinationsActivity.this, DestinationAddActivity.class);
                startActivity(intent);
            }
        });

    }
}
