package com.example.letter.Models;

public class Status {
    private String image,timeStamp;

    public Status() {
    }

    public Status(String image, String timeStamp) {
        this.image = image;
        this.timeStamp = timeStamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
