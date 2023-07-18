package com.taka.anonymousparty.models;

public class Image {
    private String imageURL;

    public Image(){}

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Image(String imageURL) {
        this.imageURL = imageURL;
    }
}
