package com.example.myplaces;

public class UserModel {

    public String fullName;
    public String email;
    public String phoneNumber;
    public int numLocationsAdded;
    public String profilePhoto;
    public int markerPriority;


    public UserModel(String fullName, String email, String phoneNumber, int numLocationsAdded) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.numLocationsAdded = numLocationsAdded;
        this.markerPriority = markerPriority;
    }

    public UserModel() {

    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getNumLocationsAdded() {
        return numLocationsAdded;
    }

    public void setNumLocationsAdded(int numLocationsAdded) {
        this.numLocationsAdded = numLocationsAdded;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public int getMarkerPriority() {
        return markerPriority;
    }

    public void setMarkerPriority(int markerPriority) {
        this.markerPriority = markerPriority;
    }
}
