package com.taka.anonymousparty.models;

public class User {

    private String id;
    private String email;
    private String username;
    private String imageProfile;
    private long timestamp;
    private long lastConnection;
    private boolean online;

    public User(){

    }

    public User(String id, String email, String username, long timestamp, long lastConnection, boolean online) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.imageProfile = imageProfile;
        this.timestamp = timestamp;
        this.lastConnection = lastConnection;
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageProfile() {return imageProfile;}

    public void setImageProfile(String imageProfile) {this.imageProfile = imageProfile;}

    public long getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(long lastConnection) {
        this.lastConnection = lastConnection;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
