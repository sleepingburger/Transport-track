package com.project.trackapp.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private User user;
    private int iconPicture;
    private String uidTag;

    public String getUidTag() {
        return uidTag;
    }

    public void setUidTag(String uidTag) {
        this.uidTag = uidTag;
    }

    public ClusterMarker(LatLng position, String title, String snippet, User user, int iconPicture, String uidTag) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.user = user;
        this.iconPicture = iconPicture;
        this.uidTag = uidTag;
    }


    public int getIconPicture() {
        return iconPicture;
    }

    public void setIconPicture(int iconPicture) {
        this.iconPicture = iconPicture;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }



}
