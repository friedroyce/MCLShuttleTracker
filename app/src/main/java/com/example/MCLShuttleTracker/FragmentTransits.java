package com.example.MCLShuttleTracker;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
 * Use the {@link FragmentTransits#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTransits extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button btnAddTransit;
    ListView lstTransits;

    ArrayList<ModelTransit> arrTansits = new ArrayList<>();


    DatabaseReference refRoot, refTransits, refSchedules, refDesinations;

    FirebaseListOptions<ModelTransit> options;

    String driverId;

    public FragmentTransits() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentTransits.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTransits newInstance(String param1, String param2) {
        FragmentTransits fragment = new FragmentTransits();
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
        View rootView = inflater.inflate(R.layout.fragment_transits, container, false);

        driverId = getActivity().getIntent().getStringExtra("driverId");

        btnAddTransit = rootView.findViewById(R.id.btnAddTransit);
        lstTransits = (ListView) rootView.findViewById(R.id.lstTransits);

        refRoot = FirebaseDatabase.getInstance().getReference();
        refTransits = refRoot.child("Transits/"+driverId);
        refSchedules = refRoot.child("Schedules");
        refDesinations = refRoot.child("Stations");

        options = new FirebaseListOptions.Builder<ModelTransit>().setQuery(refTransits.orderByChild("hour"), ModelTransit.class).setLayout(R.layout.list_item_transit).build();

        FirebaseListAdapter<ModelTransit> firebaseListAdapter = new FirebaseListAdapter<ModelTransit>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull final ModelTransit model, int position) {

                DatabaseReference itemRef = getRef(position);

                final TextView txtTime = v.findViewById(R.id.txtTime);
                final TextView txtFrom = v.findViewById(R.id.txtFrom);
                final TextView txtTo = v.findViewById(R.id.txtTo);


//                get time from sched table

                refSchedules.child(model.getSched()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String time =  dataSnapshot.child("hour").getValue() + ":" + dataSnapshot.child("minute").getValue();

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

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        //ShowToast("Failed to read database");
                    }
                });

                //get destination ref here

                refDesinations.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String from =  dataSnapshot.child(model.getFrom()).child("name").getValue().toString();
                        String to =  dataSnapshot.child(model.getTo()).child("name").getValue().toString();

                        txtFrom.setText(from);
                        txtTo.setText(to);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        //ShowToast("Failed to read database");
                    }
                });


                arrTansits.add(new ModelTransit(itemRef.getKey(), model.getDriver(), model.getSched(), model.getFrom(), model.getTo()));
            }
        };

        firebaseListAdapter.startListening();
        lstTransits.setAdapter(firebaseListAdapter);

        btnAddTransit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityTransitAdd.class);
                intent.putExtra("driverId", driverId);
                startActivity(intent);
            }
        });

        lstTransits.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                Intent intent = new Intent(getActivity(), ActivityTransitEdit.class);
                intent.putExtra("id", arrTansits.get(i).getId());
                intent.putExtra("driver", arrTansits.get(i).getDriver());
                intent.putExtra("sched", arrTansits.get(i).getSched());
                intent.putExtra("from", arrTansits.get(i).getFrom());
                intent.putExtra("to", arrTansits.get(i).getTo());
                startActivity(intent);
            }
        });

        return rootView;
    }

    void populatelist(){

    }

    void ShowToast(String message){ Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show(); }
}