package com.example.MCLShuttleTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
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

public class ActivityTransitAdd extends AppCompatActivity {

    String driverId;

    Button btnAdd, btnCancel;
    Spinner spnSchedule, spnFrom, spnTo;

    DatabaseReference refRoot, refSchedules, refStations, refTransits, refDriver;

    FirebaseListOptions<DestinationModel> optionsStation;
    DestinationModel[] stationArr;
    FirebaseListOptions<ScheduleModel> optionsSchedule;
    ScheduleModel[] scheduleArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit_add);

        driverId = getIntent().getStringExtra("driverId");

        btnAdd = findViewById(R.id.btnAdd);
        btnCancel = findViewById(R.id.btnCancel);
        spnSchedule = findViewById(R.id.spnSchedule);
        spnFrom = findViewById(R.id.spnFrom);
        spnTo = findViewById(R.id.spnTo);

        refRoot = FirebaseDatabase.getInstance().getReference();
        refSchedules = refRoot.child("Schedules");
        refStations = refRoot.child("Stations");
        refDriver = refRoot.child("Drivers/" + driverId);
        refTransits = refRoot.child("Transits");

        refSchedules.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count = String.valueOf(dataSnapshot.getChildrenCount());
                scheduleArr = new ScheduleModel[Integer.valueOf(count)];

                optionsSchedule = new FirebaseListOptions.Builder<ScheduleModel>().setQuery(refSchedules.orderByChild("hour"), ScheduleModel.class).setLayout(R.layout.spinner_item_schedule).build();

                FirebaseListAdapter<ScheduleModel> firebaseListAdapter = new FirebaseListAdapter<ScheduleModel>(optionsSchedule) {
                    @Override
                    protected void populateView(@NonNull View v, @NonNull ScheduleModel model, int position) {

                        DatabaseReference itemRef = getRef(position);

                        TextView txtTime = v.findViewById(R.id.txtTime);

                        String time = model.getHour() + ":" + model.getMinute();
                        SimpleDateFormat f24hours = new SimpleDateFormat(
                                "HH:mm"
                        );

                        final Date date;
                        try {
                            date = f24hours.parse(time);

                            final SimpleDateFormat f12hours = new SimpleDateFormat(
                                    "hh:mm aa"
                            );

                            final String time12hr = f12hours.format(date);

                            txtTime.setText(time12hr);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        scheduleArr[position] = new ScheduleModel();
                        scheduleArr[position].setId(itemRef.getKey());
                        scheduleArr[position].setHour(model.getHour());
                        scheduleArr[position].setMinute(model.getMinute());

                    }
                };

                firebaseListAdapter.startListening();
                spnSchedule.setAdapter(firebaseListAdapter);
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

                    }
                };

                firebaseListAdapter.startListening();
                spnFrom.setAdapter(firebaseListAdapter);
                spnTo.setAdapter(firebaseListAdapter);
                //spnTo.setSelection(1);
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

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(spnFrom.getSelectedItemPosition() == spnTo.getSelectedItemPosition()){
                    ShowToast("You cannot set the same stations");
                }
                else{

                    refDriver.child("transits").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()){

                                String hour = String.valueOf(scheduleArr[spnSchedule.getSelectedItemPosition()].getHour());

                                if (ds.getValue().equals(hour)){
                                    ShowToast("You already have a transit scheduled for this time");
                                    return;
                                }

                            }

                            DatabaseReference transit = refTransits.push();
                            transit.child("driver").setValue(driverId);
                            transit.child("hour").setValue(scheduleArr[spnSchedule.getSelectedItemPosition()].getHour());
                            transit.child("sched").setValue(scheduleArr[spnSchedule.getSelectedItemPosition()].getId());
                            transit.child("from").setValue(stationArr[spnFrom.getSelectedItemPosition()].getId());
                            transit.child("to").setValue(stationArr[spnTo.getSelectedItemPosition()].getId());
                            transit.child(stationArr[spnFrom.getSelectedItemPosition()].getId()).setValue(true);
                            transit.child(stationArr[spnTo.getSelectedItemPosition()].getId()).setValue(true);

                            refDriver.child("transits").child(transit.getKey()).setValue(scheduleArr[spnSchedule.getSelectedItemPosition()].getHour());

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

    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}