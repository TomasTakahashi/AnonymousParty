package com.taka.anonymousparty.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.channel.NotificationHelper;
import com.taka.anonymousparty.models.Message;
import com.taka.anonymousparty.receivers.MessageReceiver;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    public static final String NOTIFICATION_REPLY = "NotificationReply";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        if (title != null) {
            if (title.equals("New message")) {
                showNotificationMessage(data);
            }
            else {
                showNotification(title, body);
            }
        }
    }

    private void showNotification(String title, String body) {
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotification(title, body);
        Random random = new Random();
        int n = random.nextInt(10000);
        notificationHelper.getManager().notify(n, builder.build());
    }

    private void showNotificationMessage(Map <String, String> data) {
        String title = data.get("title");
        String body = data.get("body");
        String usernameSender = data.get("usernameSender");
        String userIdSender = data.get("userIdSender");
        String usernameReceiver = data.get("usernameReceiver");
        String userIdReceiver = data.get("userIdReceiver");
        String messagesJSON = data.get("messages");
        String idChat = data.get("idChat");
        int idNotification = Integer.parseInt(data.get("idNotification"));

        Intent intent = new Intent(this, MessageReceiver.class);
        intent.putExtra("userIdSender", userIdSender);
        intent.putExtra("userIdReceiver", userIdReceiver);
        intent.putExtra("idChat", idChat);
        intent.putExtra("idNotification", idNotification);
        intent.putExtra("usernameSender", usernameSender);
        intent.putExtra("usernameReceiver", usernameReceiver);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_REPLY).setLabel("Your message...").build();

        final NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "REPLY",
                pendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        Gson gson = new Gson();
        Message[] messages = gson.fromJson(messagesJSON, Message[].class);

        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder =
                notificationHelper.getNotificationMessage(
                        messages,
                        usernameSender,
                        usernameReceiver,
                        action
                );
        notificationHelper.getManager().notify(idNotification, builder.build());
    }
}