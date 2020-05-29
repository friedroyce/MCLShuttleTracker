package com.example.MCLShuttleTracker;

public class Destination {
    double latitude, longitude;
    String id, address, name;

    public Destination(){

    }

    public Destination(String i, String n, float lat, float lng, String a){
        id = i;
        latitude = lat;
        longitude = lng;
        name = n;
        address = a;
    }
}
