package com.example.letter.Models;

public class User {
    private String uid,name,phoneNumber,image_url;

    public User() {
    }

    public User(String uid, String name, String phoneNumber, String image_url) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.image_url = image_url;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
