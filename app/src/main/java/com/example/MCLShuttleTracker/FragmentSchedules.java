package com.example.MCLShuttleTracker;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSchedules#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSchedules extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    Button btnAddSchedule;
    ListView lstSchedules;

    ArrayList<ScheduleModel> schedules = new ArrayList<>();

    DatabaseReference refSchedules;

    FirebaseListOptions<ScheduleModel> options;

    public FragmentSchedules() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSchedules.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSchedules newInstance(String param1, String param2) {
        FragmentSchedules fragment = new FragmentSchedules();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_schedules, container, false);

        btnAddSchedule = rootView.findViewById(R.id.btnAddSchedule2);
        lstSchedules = rootView.findViewById(R.id.lstSchedules2);

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
                        getActivity(),
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

        return rootView;
    }

    void ShowToast(String message){ Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show(); }
}