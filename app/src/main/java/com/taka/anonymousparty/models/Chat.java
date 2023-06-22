package com.taka.anonymousparty.models;

import java.util.ArrayList;

public class Chat {

    private String idChat;
    private String idUser1;
    private String idUser2;
    private boolean isWriting;
    private long timestamp;
    private ArrayList<String> idsUsers;

    public Chat(){

    }

    public Chat(String idChat, String idUser1, String idUser2, boolean isWriting, long timestamp, ArrayList<String> idsUsers) {
        this.idChat = idChat;
        this.idUser1 = idUser1;
        this.idUser2 = idUser2;
        this.isWriting = isWriting;
        this.timestamp = timestamp;
        this.idsUsers = idsUsers;
    }

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idChat) {
        this.idChat = idChat;
    }

    public ArrayList<String> getIdsUsers() {
        return idsUsers;
    }

    public void setIdsUsers(ArrayList<String> idsUsers) {
        this.idsUsers = idsUsers;
    }

    public String getIdUser1() {
        return idUser1;
    }

    public void setIdUser1(String idUser1) {
        this.idUser1 = idUser1;
    }

    public String getIdUser2() {
        return idUser2;
    }

    public void setIdUser2(String idUser2) {
        this.idUser2 = idUser2;
    }

    public boolean isWriting() {
        return isWriting;
    }

    public void setWriting(boolean writing) {
        isWriting = writing;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}
