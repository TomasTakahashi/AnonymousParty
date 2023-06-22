package com.taka.anonymousparty.providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.taka.anonymousparty.models.Token;

public class TokenProvider {

    CollectionReference mCollectionToken;

    public TokenProvider(){
        mCollectionToken = FirebaseFirestore.getInstance().collection("Tokens");
    }

    public void create(String idUser){
        if (idUser == null){
            return;
        }
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String result) {
                Token token = new Token(result);
                mCollectionToken.document(idUser).set(token);
            }
        });
    }

    public Task<DocumentSnapshot> getToken(String idUser){
        return mCollectionToken.document(idUser).get();
    }
}
