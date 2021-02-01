package com.example.MCLShuttleTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.ArrayList;
import java.util.Date;

public class SchedulesActivity extends AppCompatActivity {

    Button btnAddSchedule;
    ListView lstSchedules;

    ArrayList<ScheduleModel> schedules = new ArrayList<>();

    DatabaseReference refSchedules;

    FirebaseListOptions<ScheduleModel> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedules);

        btnAddSchedule = findViewById(R.id.btnAddSchedule);
        lstSchedules = findViewById(R.id.lstSchedules);

        refSchedules = FirebaseDatabase.getInstance().getReference("Schedules");

        options = new FirebaseListOptions.Builder<ScheduleModel>().setQuery(refSchedules.orderByChild("hour"), ScheduleModel.class).setLayout(R.layout.list_item_schedule).build();

        FirebaseListAdapter<ScheduleModel> firebaseListAdapter = new FirebaseListAdapter<ScheduleModel>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull ScheduleModel model, int position) {

                DatabaseReference itemRef = getRef(position);

                TextView txtTime = v.findViewById(R.id.txtTime);

                String time = model.getHour() + ":" +model.getMinute();

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

                    schedules.add(new ScheduleModel(itemRef.getKey(), model.getHour(), model.getMinute()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        firebaseListAdapter.startListening();
        lstSchedules.setAdapter(firebaseListAdapter);

        btnAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    SchedulesActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            final int hour = hourOfDay;
                            final int min = minute;

//                            final String time = hour + ":" + min;
//
//                            SimpleDateFormat f24hours = new SimpleDateFormat(
//                                    "HH:mm"
//                            );
//
//                            try {
//                                final Date date = f24hours.parse(time);
//
//                                final SimpleDateFormat f12hours = new SimpleDateFormat(
//                                        "hh:mm aa"
//                                );
//
//                                final String time12hr = f12hours.format(date);
                                final DatabaseReference schedules = FirebaseDatabase.getInstance().getReference("Schedules");
                                schedules.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        DatabaseReference schedule = schedules.push();
                                        schedule.child("hour").setValue(hour);
                                        schedule.child("minute").setValue(min);

                                        ShowToast("Added Successfully");
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        ShowToast("Failed to read database");
                                    }
                                });
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
                        }
                    }, 12, 0, false
                );

                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                //displays prev selected time
                //timePickerDialog.updateTime(hrs,min);
                timePickerDialog.show();
            }
        });
    }

    void ShowToast(String message){ Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}