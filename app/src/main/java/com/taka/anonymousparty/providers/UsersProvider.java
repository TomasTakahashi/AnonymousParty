package com.taka.anonymousparty.providers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.taka.anonymousparty.models.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UsersProvider {

    private CollectionReference mCollectionUsers;
    private String randomUserId;

    public UsersProvider(){
        mCollectionUsers = FirebaseFirestore.getInstance().collection("Users");
    }

    public String getRandomUserId(){
        return randomUserId;
    }

    public Task<DocumentSnapshot> getUser(String userId) {
        return mCollectionUsers.document(userId).get();
    }

    public DocumentReference getUserRealTime(String userId) {
        return mCollectionUsers.document(userId);
    }

    public Task<Void> create(User user) {
        return mCollectionUsers.document(user.getId()).set(user);
    }

    public Task<Void> update(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("timestamp", new Date().getTime());
        map.put("imageProfile", user.getImageProfile());
        return mCollectionUsers.document(user.getId()).update(map);
    }

    public Task<Void> updateUsername(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("username", user.getUsername());
        return mCollectionUsers.document(user.getId()).update(map);
    }

    public Task<Void> updateUProfileIcon(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("imageProfile", user.getImageProfile());
        return mCollectionUsers.document(user.getId()).update(map);
    }

    public static void updateOnline(boolean status, final Context context){
        UsersProvider usersProvider = new UsersProvider();
        AuthProvider authProvider = new AuthProvider();
        usersProvider.TaskupdateOnline(authProvider.getUid(), status);
    }

    public Task<Void> TaskupdateOnline(String idUser, boolean status) {
        Map<String, Object> map = new HashMap<>();
        map.put("online", status);
        map.put("lastConnection", new Date().getTime());
        return mCollectionUsers.document(idUser).update(map);
    }

    public Task<Void> getRandomUser(CollectionReference mCollectionUsers) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        mCollectionUsers.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                int size = documents.size();

                if (size > 0) {
                    int randomIndex = new Random().nextInt(size);
                    DocumentSnapshot randomUserDocument = documents.get(randomIndex);
                    randomUserId = randomUserDocument.getId();
                    // Realiza las tareas necesarias con el usuario aleatorio

                    // Marca la tarea como completada exitosamente
                    taskCompletionSource.setResult(null);
                } else {
                    // La colección de usuarios está vacía
                    // Marca la tarea como completada con error
                    taskCompletionSource.setException(new Exception("La colección de usuarios está vacía"));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error al obtener los usuarios
                // Marca la tarea como completada con error
                taskCompletionSource.setException(e);
            }
        });
        return taskCompletionSource.getTask();
    }

}
