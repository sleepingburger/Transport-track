package com.project.trackapp;

import android.app.Application;

import com.project.trackapp.model.UserLocation;

public class UserDestination extends Application {
    private UserLocation userLocation = null;

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }
}
