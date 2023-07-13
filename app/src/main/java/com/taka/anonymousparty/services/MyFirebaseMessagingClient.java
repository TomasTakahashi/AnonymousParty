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
                notifyMessage(data);
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

//    private void showNotificationMessage(Map<String, String> data) {
//        final String imageSender = data.get("imageSender");
//        final String imageReceiver = data.get("imageReceiver");
//        Log.d("ENTRO", "NUEVO MENSAJE");
//        getImageSender(data, imageSender, imageReceiver);
//    }

//    private void getImageSender(final Map<String, String> data, final String imageSender, final String imageReceiver) {
//        new Handler(Looper.getMainLooper())
//                .post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Picasso.with(getApplicationContext())
//                                .load(imageSender)
//                                .into(new Target() {
//                                    @Override
//                                    public void onBitmapLoaded(final Bitmap bitmapSender, Picasso.LoadedFrom from) {
//                                        getImageReceiver(data, imageReceiver, bitmapSender);
//                                    }
//                                    @Override
//                                    public void onBitmapFailed(Drawable errorDrawable) {
//                                        getImageReceiver(data, imageReceiver, null);
//                                    }
//                                    @Override
//                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                                    }
//                                });
//                    }
//                });
//    }

//    private void getImageReceiver(final Map<String, String> data, String imageReceiver, final Bitmap bitmapSender) {
//        Picasso.with(getApplicationContext())
//                .load(imageReceiver)
//                .into(new Target() {
//                    @Override
//                    public void onBitmapLoaded(Bitmap bitmapReceiver, Picasso.LoadedFrom from) {
//                        notifyMessage(data, bitmapSender, bitmapReceiver);
//                    }
//                    @Override
//                    public void onBitmapFailed(Drawable errorDrawable) {
//                        notifyMessage(data, bitmapSender, null);
//                    }
//                    @Override
//                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                    }
//                });
//    }

    private void notifyMessage(Map<String, String> data) {
        final String usernameSender = data.get("usernameSender");
        final String usernameReceiver = data.get("usernameReceiver");
        final String lastMessage = data.get("lastMessage");
        String messagesJSON = data.get("messages");
//        final String imageSender = data.get("imageSender");
//        final String imageReceiver = data.get("imageReceiver");

        final String userIdSender = data.get("userIdSender");
        final String userIdReceiver = data.get("userIdReceiver");
        final String idChat = data.get("idChat");
        final int idNotification = Integer.parseInt(data.get("idNotification"));

        Intent intent = new Intent(this, MessageReceiver.class);
        intent.putExtra("userIdSender", userIdSender);
        intent.putExtra("userIdReceiver", userIdReceiver);
        intent.putExtra("idChat", idChat);
        intent.putExtra("idNotification", idNotification);
        intent.putExtra("usernameSender", usernameSender);
        intent.putExtra("usernameReceiver", usernameReceiver);
//        intent.putExtra("imageSender", imageSender);
//        intent.putExtra("imageReceiver", imageReceiver);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_REPLY).setLabel("Your message...").build();

        final NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "REPLY",
                pendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        Gson gson = new Gson();
        final Message[] messages = gson.fromJson(messagesJSON, Message[].class);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder =
                notificationHelper.getNotificationMessage(
                        messages,
                        usernameSender,
                        usernameReceiver,
                        lastMessage,
//                        bitmapSender,
//                        bitmapReceiver,
                        action
                );
        notificationHelper.getManager().notify(idNotification, builder.build());
    }
}