package com.example.letter.AddUserRoomArchitecture;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import javax.xml.namespace.NamespaceContext;

@Entity(tableName = "USER_CONTACT_INFORMATION")
public class AddUserEntity {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "UID")
    private String uid;

    @ColumnInfo(name = "NAME")
    private String name;

    @ColumnInfo(name = "IMAGE URL")
    private String image_url;

    @ColumnInfo(name = "PHONE NUMBER")
    private String phoneNumber;

    @Ignore
    public AddUserEntity() {
    }

    public AddUserEntity(@NonNull String uid, String name, String image_url, String phoneNumber) {
        this.uid = uid;
        this.name = name;
        this.image_url = image_url;
        this.phoneNumber = phoneNumber;
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

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
