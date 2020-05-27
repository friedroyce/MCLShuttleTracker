package com.example.MCLShuttleTracker;

import android.app.Application;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MCLShuttleTracker extends Application {

    FirebaseDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();

        database = FirebaseDatabase.getInstance();

        final DatabaseReference rootRef = database.getReference();
        DatabaseReference Students = rootRef.child("Students");



    }
}
