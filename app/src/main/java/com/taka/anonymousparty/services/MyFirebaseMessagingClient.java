package com.taka.anonymousparty.services;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.taka.anonymousparty.channel.NotificationHelper;

import java.util.Map;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String, String> data = message.getData();
        String titleNotification = data.get("titleNotification");
        String bodyNotification = data.get("bodyNotification");

        if (titleNotification != null){
            showNotification(titleNotification, bodyNotification);
        }
    }

    private void showNotification(String title, String body){
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotification(title, body);
        notificationHelper.getManager().notify(1, builder.build());
    }

}