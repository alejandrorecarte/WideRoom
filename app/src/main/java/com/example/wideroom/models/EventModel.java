package com.example.wideroom.models;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * This class is the model for events.
 *
 * Copyright © 2024 Alejandro Recarte Rebollo & Inés Rodrigues Trigo. CC BY-NC (Attribution-NonCommercial)
 *
 * @author Alejandro Recarte Rebollo <alejandro.recarte.rebollo@gmail.com>+
 * @author Inés Rodrigues Trigo <itralways@gmail.com>
 *
 * @version 1.0
 * @date 08-06-2024
 */

public class EventModel implements Serializable {
    private String eventId;
    private String eventName;
    private String date;
    private String city;
    private String address;
    private double distanceInM;
    private double lat;
    private double lng;
    private String category;

    /**
     * Empty constructor.
     */
    public EventModel() {
    }

    /**
     * Returns the event id.
     * @return eventId
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the event id.
     * @param eventId
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the event name.
     * @return eventName
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the event name.
     * @param eventName
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Gets the date.
     * @return date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date.
     * @param date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Returns the city.
     * @return city
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Returns the address.
     * @return address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address.
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the distance in meters.
     * @return distanceInM
     */
    public double getDistanceInM() {
        return distanceInM;
    }

    /**
     * Sets the distance in meters.
     * @param distanceInM
     */
    public void setDistanceInM(double distanceInM) {
        this.distanceInM = distanceInM;
    }

    /**
     * Returns the latitude.
     * @return lat
     */
    public double getLat() {
        return lat;
    }

    /**
     * Sets the latitude.
     * @param lat
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * Returns the longitude.
     * @return lng
     */
    public double getLng() {
        return lng;
    }

    /**
     * Sets the longitude.
     * @param lng
     */
    public void setLng(double lng) {
        this.lng = lng;
    }

    /**
     * Returnst the distance between the user an the event as a string with the unit "km".
     * @return distanceInKm
     */
    public String getDistanceAsString() {
        // convert the distance from meters to kilometers
        double distanceInKm = distanceInM / 1000.0;
        // format the distance to two decimal places
        DecimalFormat df = new DecimalFormat("#.#");
        String formattedDistance = df.format(distanceInKm);
        // add the unit "km" to the formatted distance
        return formattedDistance + " km";
    }

    /**
     * Returns the category.
     * @return category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category.
     * @param category
     */
    public void setCategory(String category) {
        this.category = category;
    }
}