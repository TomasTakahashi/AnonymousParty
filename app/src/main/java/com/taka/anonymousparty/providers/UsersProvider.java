package com.taka.anonymousparty.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.taka.anonymousparty.models.User;

import java.util.HashMap;
import java.util.Map;

public class UsersProvider {

    private CollectionReference mCollectionUsers;

    public UsersProvider(){
        mCollectionUsers = FirebaseFirestore.getInstance().collection("Users");
    }

    public Task<DocumentSnapshot> getUser(String userId) {
        return mCollectionUsers.document(userId).get();
    }

    public Task<Void> create(User user) {
        return mCollectionUsers.document(user.getId()).set(user);
    }

    public Task<Void> update(User user){
        Map<String, Object> mapUserInfo = new HashMap<>();
        mapUserInfo.put("username", user.getUsername());
        return mCollectionUsers.document(user.getId()).update(mapUserInfo);
    }

}
