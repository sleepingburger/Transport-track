package com.project.trackapp.model;

import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Messages{
    private User user;
    private String message;
    private String title;
    private String message_id;
    private @ServerTimestamp
    Date timestamp;

    public Messages(User user, String message, String title, String message_id, Date timestamp) {
        this.user = user;
        this.message = message;
        this.title = title;
        this.message_id = message_id;
        this.timestamp = timestamp;
    }

    public Messages(){}


    public void setUser(User user) {
        this.user = user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage_id() {
        return message_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Messages{" +
                "user=" + user +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", message_id='" + message_id + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
