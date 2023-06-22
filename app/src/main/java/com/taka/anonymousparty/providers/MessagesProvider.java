package com.taka.anonymousparty.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.taka.anonymousparty.models.Message;

public class MessagesProvider {

    CollectionReference mCollection;

    public MessagesProvider(){
        mCollection = FirebaseFirestore.getInstance().collection("Messages");
    }

    public Task<Void> create(Message message){
        DocumentReference document = mCollection.document();
        message.setIdMessage(document.getId());
        return document.set(message);
    }

}
