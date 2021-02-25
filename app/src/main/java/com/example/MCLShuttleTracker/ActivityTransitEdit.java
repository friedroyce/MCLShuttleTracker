package com.example.MCLShuttleTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityTransitEdit extends AppCompatActivity {

    String driverId, transitId, schedID, fromID, toID;
    int schedPos = 0, fromPos = 0, toPos = 0, hour = 0;

    Button btnSave, btnCancel, btnDelete;
    Spinner spnSchedule, spnFrom, spnTo;
    TextView txtSchedule;

    DatabaseReference refRoot, refSchedules, refStations, refTransits, refDriver;

    FirebaseListOptions<DestinationModel> optionsStation;
    DestinationModel[] stationArr;
    FirebaseListOptions<ScheduleModel> optionsSchedule;
    ScheduleModel[] scheduleArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit_edit);

        driverId = getIntent().getStringExtra("driver");
        transitId = getIntent().getStringExtra("id");
        schedID = getIntent().getStringExtra("sched");
        fromID = getIntent().getStringExtra("from");
        toID = getIntent().getStringExtra("to");

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);
        txtSchedule = findViewById(R.id.txtSchedule);
        spnFrom = findViewById(R.id.spnFrom);
        spnTo = findViewById(R.id.spnTo);

        refRoot = FirebaseDatabase.getInstance().getReference();
        refSchedules = refRoot.child("Schedules");
        refStations = refRoot.child("Stations");
        refDriver = refRoot.child("Drivers/" + driverId);
        refTransits = refRoot.child("Transits");

        refSchedules.child(schedID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                hour = Integer.parseInt(dataSnapshot.child("hour").getValue().toString());

                String time = dataSnapshot.child("hour").getValue() + ":" + dataSnapshot.child("minute").getValue();
                SimpleDateFormat f24hours = new SimpleDateFormat("HH:mm");

                final Date date;
                try {
                    date = f24hours.parse(time);

                    final SimpleDateFormat f12hours = new SimpleDateFormat("hh:mm aa");

                    final String time12hr = f12hours.format(date);

                    txtSchedule.setText(time12hr);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

        refStations.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count = String.valueOf(dataSnapshot.getChildrenCount());
                stationArr = new DestinationModel[Integer.valueOf(count)];

                optionsStation = new FirebaseListOptions.Builder<DestinationModel>().setQuery(refStations, DestinationModel.class).setLayout(R.layout.spinner_item_destination).build();

                FirebaseListAdapter<DestinationModel> firebaseListAdapter = new FirebaseListAdapter<DestinationModel>(optionsStation) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull DestinationModel model, int position) {

                        DatabaseReference itemRef = getRef(position);

                        TextView txtName = v.findViewById(R.id.txtDestinationName);

                        txtName.setText(model.getName());
                        stationArr[position] = new DestinationModel();
                        stationArr[position].setId(itemRef.getKey());
                        stationArr[position].setName(model.getName());
                        stationArr[position].setLatitude(model.getLatitude());
                        stationArr[position].setLongitude(model.getLongitude());
                        stationArr[position].setAddress(model.getAddress());

                        if(stationArr[position].getId() == fromID){
                            fromPos = position;
                            spnFrom.setSelection(fromPos);
                            ShowToast("true");
                        }
                        if(stationArr[position].getId() == toID){
                            toPos = position;
                            spnTo.setSelection(toPos);
                        }

                    }
                };

                firebaseListAdapter.startListening();
                spnFrom.setAdapter(firebaseListAdapter);
                spnTo.setAdapter(firebaseListAdapter);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                //ShowToast("Failed to read database");
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityTransitEdit.this);
                builder.setMessage("Are you sure you want to delete this transit schedule?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                refDriver.child("transits").child(transitId).removeValue();
                                refSchedules.child(schedID).child("transits").child(transitId).removeValue();
                                refTransits.child(transitId).removeValue();

                                ShowToast("Location Deleted Successfully");
                                finish();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(spnFrom.getSelectedItemPosition() == spnTo.getSelectedItemPosition()){
                    ShowToast("You cannot set the same stations");
                }
                else{
                    refDriver.child("transits").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            DatabaseReference transit = refTransits.child(transitId);
                            transit.child("driver").setValue(driverId);
                            transit.child("hour").setValue(hour);
                            transit.child("sched").setValue(schedID);
                            transit.child("from").setValue(stationArr[spnFrom.getSelectedItemPosition()].getId());
                            transit.child("to").setValue(stationArr[spnTo.getSelectedItemPosition()].getId());
                            transit.child(stationArr[spnFrom.getSelectedItemPosition()].getId()).setValue(true);
                            transit.child(stationArr[spnTo.getSelectedItemPosition()].getId()).setValue(true);

                            refSchedules.child(schedID).child("transits").child(transit.getKey()).setValue(scheduleArr[spnSchedule.getSelectedItemPosition()].getHour());
                            refDriver.child("transits").child(transit.getKey()).setValue(hour);

                            ShowToast("Added Successfully");
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

        //spnSchedule.setSelection(schedPos);
        spnFrom.setSelection(fromPos);
        spnTo.setSelection(toPos);
    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}