package com.example.wideroom.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.text.DecimalFormat;

public class EventModel implements Serializable {
    private String eventId;
    private String eventName;
    private String date;
    private String city;
    private String address;
    private double distanceInM;
    private double lat;
    private double lng;

    public EventModel() {
    }

    public EventModel(String eventId, String eventName, String date, String city, String address) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.date = date;
        this.city = city;
        this.address = address;
    }

    public EventModel(String eventId, String eventName, String date, String city, String address, double distanceInM, double lat, double lng) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.date = date;
        this.city = city;
        this.address = address;
        this.distanceInM = distanceInM;
        this.lat = lat;
        this.lng = lng;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public double getDistanceInM() {
        return distanceInM;
    }

    public void setDistanceInM(double distanceInM) {
        this.distanceInM = distanceInM;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getDistanceAsString() {
        // Convertir la distancia de metros a kilómetros
        double distanceInKm = distanceInM / 1000.0;

        // Formatear la distancia con dos decimales
        DecimalFormat df = new DecimalFormat("#.#");
        String formattedDistance = df.format(distanceInKm);

        // Agregar la unidad de medida "km" al String formateado
        return formattedDistance + " km";
    }
}
