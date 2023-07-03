package com.taka.anonymousparty.receivers;

import static com.taka.anonymousparty.services.MyFirebaseMessagingClient.NOTIFICATION_REPLY;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.taka.anonymousparty.models.FCMBody;
import com.taka.anonymousparty.models.FCMResponse;
import com.taka.anonymousparty.models.Message;
import com.taka.anonymousparty.providers.MessagesProvider;
import com.taka.anonymousparty.providers.NotificationProvider;
import com.taka.anonymousparty.providers.TokenProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageReceiver extends BroadcastReceiver {

    String mExtraUserIdSender;
    String mExtraUserIdReceiver;
    String mExtraIdChat;
    String mExtraUsernameSender;
    String mExtraUsernameReceiver;
    int mExtraIdNotification;

    TokenProvider mTokenProvider;

    NotificationProvider mNotificationProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        mExtraUserIdSender = intent.getExtras().getString("userIdSender");
        mExtraUserIdReceiver = intent.getExtras().getString("userIdReceiver");
        mExtraUsernameSender = intent.getExtras().getString("usernameSender");
        mExtraUsernameReceiver = intent.getExtras().getString("usernameReceiver");
        mExtraIdChat = intent.getExtras().getString("idChat");
        mExtraIdNotification = intent.getExtras().getInt("idNotification");

        mTokenProvider = new TokenProvider();
        mNotificationProvider = new NotificationProvider();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mExtraIdNotification);

        String message = getMessageText(intent).toString();

        sendMessage(message);
    }

    private void sendMessage(String textMessage) {
        final Message message = new Message();

        message.setUserIdSender(mExtraUserIdReceiver);
        message.setUserIdReceiver(mExtraUserIdSender);
        message.setTimestamp(new Date().getTime());
        message.setViewed(false);
        message.setIdChat(mExtraIdChat);
        message.setMessage(textMessage);

        MessagesProvider messagesProvider = new MessagesProvider();

        messagesProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    getToken(message);
                }
            }
        });
    }

    private CharSequence getMessageText(Intent intent){
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null){
            return remoteInput.getCharSequence(NOTIFICATION_REPLY);
        }
        return null;
    }

    private void getToken(Message message){
        mTokenProvider.getToken(mExtraUserIdSender).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if(documentSnapshot.contains("token")){
                        String token = documentSnapshot.getString("token");
                        Gson gson = new Gson();
                        ArrayList<Message> messageArrayList = new ArrayList<>();
                        messageArrayList.add(message);
                        String messages = gson.toJson(messageArrayList);
                        sendNotification(token, messages, message);
                    }
                }
            }
        });
    }

    private void sendNotification(final String token, String messages, Message message) {
        final Map<String, String> data = new HashMap<>();
        data.put("title", "NUEVO MENSAJE");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mExtraIdNotification));
        data.put("messages", messages);
        data.put("usernameSender", mExtraUserIdReceiver.toUpperCase());
        data.put("userIdSender", message.getUserIdSender());
        data.put("usernameReceiver", mExtraUsernameSender.toUpperCase());
        data.put("userIdReceiver", message.getUserIdReceiver());
        data.put("idChat", message.getIdChat());

        FCMBody body = new FCMBody(token, "high", "4500s", data);
        mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Log.d("ERROR ONFAILURE", "Error al enviar la notificación: " + t.getMessage());
            }
        });
    }
}
