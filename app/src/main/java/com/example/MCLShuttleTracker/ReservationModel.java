package com.example.MCLShuttleTracker;

public class ReservationModel {
    private double accuracy, latitude, longitude;
    private String altitude, speed, address, name;

    public ReservationModel() {

    }

    public ReservationModel(double accuracy, String altitude, double latitude, double longitude, String speed, String address, String name) {
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.address = address;
        this.name = name;
    }


    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
