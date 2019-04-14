package com.project.trackapp.model;

public class Customer {
    private int deliveryNumber;
    private String firstName;
    private String lastName;
    private String address;
    private long mobile;
    private String email;
    private String description;

    public long getMobile() {
        return mobile;
    }

    public void setMobile(long mobile) {
        this.mobile = mobile;
    }

    public Customer(){}
    public Customer(int deliveryNumber, String firstName, String lastName, String address, long mobile, String email,String description) {
        this.deliveryNumber = deliveryNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.mobile = mobile;
        this.email = email;
        this.description = description;
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
}
