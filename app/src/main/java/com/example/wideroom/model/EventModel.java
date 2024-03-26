package com.example.wideroom.model;

import com.google.firebase.Timestamp;

public class EventModel {
    private String eventId;
    private String eventName;
    private String date;
    private String city;

    public EventModel() {
    }

    public EventModel(String eventId, String eventName, String date, String city) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.date = date;
        this.city = city;
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
}
