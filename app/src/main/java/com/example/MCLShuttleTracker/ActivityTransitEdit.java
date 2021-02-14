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
    int schedPos = 0, fromPos = 0, toPos = 0;

    Button btnSave, btnCancel, btnDelete;
    Spinner spnSchedule, spnFrom, spnTo;

    DatabaseReference refRoot, refSchedules, refStations, refTransits;

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

        //ShowToast(toID);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);
        spnSchedule = findViewById(R.id.spnSchedule);
        spnFrom = findViewById(R.id.spnFrom);
        spnTo = findViewById(R.id.spnTo);

        refRoot = FirebaseDatabase.getInstance().getReference();
        refSchedules = refRoot.child("Schedules");
        refStations = refRoot.child("Stations");
        refTransits = refRoot.child("Transits/" + driverId);

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

//                for(int i = 0; i < scheduleArr.length; i++){
//                    if(scheduleArr[i].getId() == schedID)
//                        schedPos = i;
//                }

//                spnSchedule.setSelection(schedPos);
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
//                for(int i = 0; i < stationArr.length; i++){
////                    if(stationArr[i].getId() == fromID)
////                        fromPos = i;
//                    if(stationArr[i].getId() == toID)
//                        toPos = i;
//                }

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
                    refTransits.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for(DataSnapshot ds : dataSnapshot.getChildren()){

                                String schedId = ds.child("sched").getValue().toString();
                                String driver = ds.child("driver").getValue().toString();

                                if (schedId == scheduleArr[spnSchedule.getSelectedItemPosition()].getId() && driverId == driver){
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

        spnSchedule.setSelection(schedPos);
        spnFrom.setSelection(fromPos);
        spnTo.setSelection(toPos);
    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}