package com.example.myplaces;

public class MarkerModel {

    private String country;
    private String street;
    private String zipCode;
    private String markerPhotoUrl;
    private String markerId;
    private int markerPriority;

    public MarkerModel() {

    }

    public MarkerModel( String markerId, String country, String street, String zipCode ) {
        this.country = country;
        this.street = street;
        this.zipCode = zipCode;
        this.markerPhotoUrl = markerPhotoUrl;
        this.markerId=markerId;
        this.markerPriority = markerPriority;
    }


    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getMarkerPhotoUrl() {
        return markerPhotoUrl;
    }

    public void setMarkerPhotoUrl(String markerPhotoUrl) {
        this.markerPhotoUrl = markerPhotoUrl;
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }


    public int getMarkerPriority() {
        return markerPriority;
    }

    public void setMarkerPriority(int markerPriority) {
        this.markerPriority = markerPriority;
    }
}
