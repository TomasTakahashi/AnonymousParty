package com.taka.anonymousparty.providers;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.taka.anonymousparty.activities.ChatActivity;
import com.taka.anonymousparty.models.Chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatsProvider {

    CollectionReference mCollectionChats;

    public ChatsProvider(){
        mCollectionChats = FirebaseFirestore.getInstance().collection("Chats");
    }

    public void create(Chat chat){
        mCollectionChats.document(chat.getIdUser1() + chat.getIdUser2()).set(chat);
    }

    public Query getAll(String idUser){
        return mCollectionChats.whereArrayContains("idsUsers", idUser);
    }

    public Query getChatByUser1AndUser2(String idUser1, String idUser2){
        ArrayList <String> idChats = new ArrayList<>();
        idChats.add(idUser1 + idUser2);
        idChats.add(idUser2 + idUser1);
        return mCollectionChats.whereIn("idChat", idChats);
    }

    public Query getLastDateMessage(String idChat) {
        return mCollectionChats.whereEqualTo("idChat", idChat);
    }

    public interface ChatExistsCallback {
        void onChatExists(boolean exists);
    }

    public void checkIfChatExists(String user1Id, String user2Id, ChatExistsCallback callback) {
        getChatByUser1AndUser2(user1Id, user2Id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int size = queryDocumentSnapshots.size();
                callback.onChatExists(size > 0);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onChatExists(false);
            }
        });
    }

    public void updateLastMessageTime(String chatId, long timestamp) {
        mCollectionChats.document(chatId)
                .update("lastMessageTime", timestamp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ChatsProvider", "Last message time updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ChatsProvider", "Failed to update last message time", e);
                    }
                });
    }
}
