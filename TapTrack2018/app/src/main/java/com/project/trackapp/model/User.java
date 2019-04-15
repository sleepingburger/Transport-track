package com.project.trackapp.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String email;
    private Boolean status;
    private String uid;
    //empty constructor
    public User(){

    }
    public User(String email, Boolean status,String uid) {
        this.email = email;
        this.status = status;
        this.uid = uid;
    }

    protected User(Parcel in) {
        email = in.readString();
        byte tmpStatus = in.readByte();
        status = tmpStatus == 0 ? null : tmpStatus == 1;
        uid = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeByte((byte) (status == null ? 0 : status ? 1 : 2));
        dest.writeString(uid);
    }
}