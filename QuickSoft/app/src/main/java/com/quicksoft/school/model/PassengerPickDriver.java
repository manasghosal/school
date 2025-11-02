package com.quicksoft.school.model;

import java.util.ArrayList;

public class PassengerPickDriver {
    private double lon;
    private double lat;
    private String placeName;
    private ArrayList<Passanger> passengerPickDriverArrayList;
    private int sequence;

    public PassengerPickDriver(double lat, double lon, String placeName, ArrayList<Passanger> passengerPickDriverArrayList, int sequence) {
        this.lon = lon;
        this.lat = lat;
        this.placeName = placeName;
        this.passengerPickDriverArrayList = passengerPickDriverArrayList;
        this.sequence = sequence;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public String getPlaceName() {
        return placeName;
    }

    public ArrayList<Passanger> getPassengerPickDriverArrayList() {
        return passengerPickDriverArrayList;
    }

    public void setPassengerPickDriverArrayList(ArrayList<Passanger> passengerPickDriverArrayList) {
        this.passengerPickDriverArrayList = passengerPickDriverArrayList;
    }

    public int getSequence(){
        return sequence;
    }
}
