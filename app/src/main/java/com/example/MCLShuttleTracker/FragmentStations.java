package com.example.MCLShuttleTracker;

import android.content.Intent;
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

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentStations#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentStations extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    Button btnAddStation;
    ListView lstStations;

    ArrayList<DestinationModel> destinations = new ArrayList<>();

    DatabaseReference refDestinations;

    FirebaseListOptions<DestinationModel> options;

    public FragmentStations() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentStations.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentStations newInstance(String param1, String param2) {
        FragmentStations fragment = new FragmentStations();
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
        View rootView = inflater.inflate(R.layout.fragment_stations, container, false);

        btnAddStation = rootView.findViewById(R.id.btnAddStation);
        lstStations = rootView.findViewById(R.id.lstStations);

        refDestinations = FirebaseDatabase.getInstance().getReference("Stations");

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
        lstStations.setAdapter(firebaseListAdapter);

        lstStations.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                Intent intent = new Intent(getActivity(), DestinationEditActivity.class);
                intent.putExtra("destinationId", destinations.get(i).getId());
                intent.putExtra("destinationName", destinations.get(i).getName());
                intent.putExtra("destinationAddress", destinations.get(i).getAddress());
                intent.putExtra("destinationLatitude", destinations.get(i).getLatitude());
                intent.putExtra("destinationLongitude", destinations.get(i).getLongitude());
                startActivity(intent);
            }
        });

        btnAddStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DestinationAddActivity.class);
//                intent.putExtra("mode", mode);
                startActivity(intent);
            }
        });

        return rootView;
    }
}