package com.project.trackapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Customer implements Parcelable {
    private int deliveryNumber;
    private String firstName;
    private String lastName;
    private String address;
    private long mobile;
    private String email;
    private String description;
    private @ServerTimestamp
    Date timestamp;

    protected Customer(Parcel in) {
        deliveryNumber = in.readInt();
        firstName = in.readString();
        lastName = in.readString();
        address = in.readString();
        mobile = in.readLong();
        email = in.readString();
        description = in.readString();
    }

    public static final Creator<Customer> CREATOR = new Creator<Customer>() {
        @Override
        public Customer createFromParcel(Parcel in) {
            return new Customer(in);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public long getMobile() {
        return mobile;
    }

    public void setMobile(long mobile) {
        this.mobile = mobile;
    }

    public Customer(){}
    public Customer(int deliveryNumber, String firstName, String lastName, String address, long mobile, String email,String description,Date timestamp) {
        this.deliveryNumber = deliveryNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.mobile = mobile;
        this.email = email;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDeliveryNumber() {
        return deliveryNumber;
    }

    public void setDeliveryNumber(int deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "deliveryNumber=" + deliveryNumber +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                ", mobile=" + mobile +
                ", email='" + email + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(deliveryNumber);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(address);
        dest.writeLong(mobile);
        dest.writeString(email);
        dest.writeString(description);
    }
}
