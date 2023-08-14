package com.taka.anonymousparty.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.activities.ChatActivity;
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

    private void showNotificationMessage(Map<String, String> data) {
        final String imageSender = data.get("imageSender");
        final String imageReceiver = data.get("imageReceiver");
        getImageSender(data, imageSender, imageReceiver);
    }

    private void getImageSender(final Map<String, String> data, final String imageSender, final String imageReceiver) {
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(imageSender)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap bitmapSender, @Nullable Transition<? super Bitmap> transition) {
                                        getImageReceiver(data, imageReceiver, bitmapSender);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                        getImageReceiver(data, imageReceiver, null);
                                    }
                                });
                    }
                });
    }

    private void getImageReceiver(final Map<String, String> data, String imageReceiver, final Bitmap bitmapSender) {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(imageReceiver)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmapReceiver, @Nullable Transition<? super Bitmap> transition) {
                        notifyMessage(data, bitmapSender, bitmapReceiver);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        notifyMessage(data, bitmapSender, null);
                    }
                });
    }

    private void notifyMessage(Map<String, String> data, Bitmap bitmapSender, Bitmap bitmapReceiver) {
        final String usernameSender = data.get("usernameSender");
        final String usernameReceiver = data.get("usernameReceiver");
        final String lastMessage = data.get("lastMessage");
        String messagesJSON = data.get("messages");
        final String imageSender = data.get("imageSender");
        final String imageReceiver = data.get("imageReceiver");
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
        intent.putExtra("imageSender", imageSender);
        intent.putExtra("imageReceiver", imageReceiver);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        RemoteInput remoteInput = new RemoteInput.Builder(NOTIFICATION_REPLY).setLabel("Your message...").build();

        final NotificationCompat.Action actionReply = new NotificationCompat.Action.Builder(
                R.mipmap.ic_app,
                "REPLY",
                pendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        Gson gson = new Gson();
        final Message[] messages = gson.fromJson(messagesJSON, Message[].class);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());


        Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
        chatIntent.putExtra("idUser1", userIdSender);
        chatIntent.putExtra("idUser2", userIdReceiver);
        chatIntent.putExtra("idChat", idChat);
        chatIntent.putExtra("idNotification", idNotification);
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), idNotification, chatIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder =
                notificationHelper.getNotificationMessage(
                        messages,
                        usernameSender,
                        usernameReceiver,
                        lastMessage,
                        bitmapSender,
                        bitmapReceiver,
                        actionReply,
                        contentIntent
                );
        notificationHelper.getManager().notify(idNotification, builder.build());
    }
}