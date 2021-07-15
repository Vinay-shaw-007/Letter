package com.example.letter.UserRoomArchitecture;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "USER_INFORMATION")
public class UserEntity {

    @PrimaryKey
    @ColumnInfo(name = "UID")
    @NonNull
    public String uid;

    @ColumnInfo(name = "NAME")
    public String name;

    @ColumnInfo(name = "PHONE NUMBER")
    public String phoneNumber;

    @ColumnInfo(name = "IMAGE URL")
    public String image_url;
    @Ignore
    public UserEntity() {
    }

    public UserEntity(@NonNull String uid, String name, String phoneNumber, String image_url) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.image_url = image_url;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
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
