package com.example.letter.Models;

import java.io.Serializable;

public class GroupMembers {
    private String uid,name,phoneNumber,image_url;
    private boolean isAdmin;

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public GroupMembers() {
    }

    public GroupMembers(String uid, String name, String phoneNumber, String image_url) {
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
