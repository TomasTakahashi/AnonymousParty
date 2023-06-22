package com.taka.anonymousparty.models;

public class Message {

    private String idMessage;
    private String idChat;
    private String userIdSender;
    private String userIdReceiver;
    private String message;
    private long timestamp;
    private boolean viewed;

    public Message(){

    }

    public Message(String idMessage, String idChat, String userIdSender, String userIdReceiver, String message, long timestamp, boolean viewed) {
        this.idMessage = idMessage;
        this.idChat = idChat;
        this.userIdSender = userIdSender;
        this.userIdReceiver = userIdReceiver;
        this.message = message;
        this.timestamp = timestamp;
        this.viewed = viewed;
    }

    public String getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idChat) {
        this.idChat = idChat;
    }

    public String getUserIdSender() {
        return userIdSender;
    }

    public void setUserIdSender(String userIdSender) {
        this.userIdSender = userIdSender;
    }

    public String getUserIdReceiver() {
        return userIdReceiver;
    }

    public void setUserIdReceiver(String userIdReceiver) {
        this.userIdReceiver = userIdReceiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }
}
