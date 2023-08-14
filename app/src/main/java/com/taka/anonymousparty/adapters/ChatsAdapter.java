package com.taka.anonymousparty.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.activities.ChatActivity;
import com.taka.anonymousparty.activities.HomeActivity;
import com.taka.anonymousparty.models.Chat;
import com.taka.anonymousparty.models.Message;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.ChatsProvider;
import com.taka.anonymousparty.providers.MessagesProvider;
import com.taka.anonymousparty.providers.UsersProvider;
import com.taka.anonymousparty.utils.RelativeTime;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends FirestoreRecyclerAdapter<Chat, ChatsAdapter.ViewHolder> {

    Context context;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    ChatsProvider mChatsProvider;
    MessagesProvider mMessagesProvider;
    ListenerRegistration mListener;

    public ChatsAdapter(FirestoreRecyclerOptions<Chat> options, Context context) {
        super(options);
        this.context = context;
        mUsersProvider = new UsersProvider();
        mAuthProvider = new AuthProvider();
        mChatsProvider = new ChatsProvider();
        mMessagesProvider = new MessagesProvider();
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull final Chat chat) {

        DocumentSnapshot document = getSnapshots().getSnapshot(position);
        final String chatId = document.getId();
        if (mAuthProvider.getUid().equals(chat.getIdUser1())) {
            getUserInfo(chat.getIdUser2(), holder);
        }
        else {
            getUserInfo(chat.getIdUser1(), holder);
        }

        holder.viewHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToChatActivity(chatId, chat.getIdUser1(), chat.getIdUser2(), chat.getIdNotificationChat());
            }
        });

        holder.viewHolder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("CLICK LARCO","CLICK LARGO");
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogChat);
                builder.setTitle("ELIMINAR CHAT CON");
                builder.setMessage("¿Estás seguro de que quieres eliminar este chat?");

                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Acción a realizar al hacer clic en "Aceptar"
                        Log.d("CLICK LARCO","" + chatId);
                        deleteChat(chatId);

                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        String idSender = "";
        if (mAuthProvider.getUid().equals(chat.getIdUser1())){
            idSender = chat.getIdUser2();
        }
        else{
            idSender = chat.getIdUser1();
        }

        getProfileIcon(idSender,holder.circleProfileIcon);
        getLastMessage(chatId, holder.textViewLastMessage, holder.imageViewViewed);
        getDateLastMessage(chatId, holder.textViewDateLastMessage);
        getMessageNotRead(chatId, idSender, holder.textViewMessageNotRead, holder.mFrameLayoutMessageNotRead);

    }

    public ListenerRegistration getListener(){
        return mListener;
    }

    private void getLastMessage(String chatId, TextView textViewLastMessage, ImageView imageViewViewed) {
        mListener = mMessagesProvider.getLastMessage(chatId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            Message lastMessage = queryDocumentSnapshots.getDocuments().get(0).toObject(Message.class);
                            if (lastMessage != null) {
                                String messageText = lastMessage.getMessage();
                                textViewLastMessage.setText(messageText);

                                if (lastMessage.getUserIdSender().equals(mAuthProvider.getUid())){
                                    imageViewViewed.setVisibility(View.VISIBLE);
                                    if (lastMessage.isViewed()){
                                        imageViewViewed.setImageResource(R.drawable.ic_double_check_night);
                                    }
                                    else{
                                        imageViewViewed.setImageResource(R.drawable.ic_single_check_night);
                                    }
                                }
                                else{
                                    imageViewViewed.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });
    }

    private void getDateLastMessage(String chatId, TextView textViewDateLastMessage){
        mListener = mChatsProvider.getLastDateMessage(chatId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            Chat chat = queryDocumentSnapshots.getDocuments().get(0).toObject(Chat.class);
                            if (chat != null) {
                                Long timestamp = chat.getLastMessageTime();
                                String relativeTime = RelativeTime.timeFormatAMPM(timestamp, context);
                                textViewDateLastMessage.setText(relativeTime);
                            }
                        }
                    }
                });
    }

    private void getMessageNotRead(String chatId, String idSender, TextView textViewMessageNotRead, FrameLayout mFrameLayoutMessageNotRead) {
        mListener = mMessagesProvider.getMessagesByChatAndSender(chatId, idSender)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        if (queryDocumentSnapshots != null){
                            int size = queryDocumentSnapshots.size();
                            if (size > 0){
                                mFrameLayoutMessageNotRead.setVisibility(View.VISIBLE);
                                textViewMessageNotRead.setText(String.valueOf(size));
                            }
                            else{
                                mFrameLayoutMessageNotRead.setVisibility(View.GONE);
                            }
                        }
                    }
        });
    }

    private void getProfileIcon(String idSender, CircleImageView mCircleProfileIcon){
        mListener = mUsersProvider.getUserRealTime(idSender).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("imageProfile")) {
                        String imageProfile = documentSnapshot.getString("imageProfile");
                        if (imageProfile != null) {
                            if (!imageProfile.equals("")) {
                                Glide.with(context).load(imageProfile).into(mCircleProfileIcon);
                            }
                        }
                        else{
                            mCircleProfileIcon.setImageResource(R.drawable.ic_person);
                        }
                    }
                }
            }
        });
    }

    private void goToChatActivity(String chatId, String idUser1, String idUser2, int idNotificationChat) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("idChat", chatId);
        intent.putExtra("idUser1", idUser1);
        intent.putExtra("idUser2", idUser2);
        intent.putExtra("idNotificationChat", idNotificationChat);
        context.startActivity(intent);
    }

    private void getUserInfo(String idUser, final ViewHolder holder) {
        mUsersProvider.getUser(idUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");
                        holder.textViewUsername.setText(username);
                    }
                    if (documentSnapshot.contains("imageProfile")) {
                        String imageProfile = documentSnapshot.getString("imageProfile");
                        if (imageProfile != null) {
                            if (!imageProfile.isEmpty()) {
                                Glide.with(context).load(imageProfile).into(holder.circleProfileIcon);
                            }
                        }
                        else{
                            holder.circleProfileIcon.setImageResource(R.drawable.ic_person);
                        }
                    }
                }
            }
        });
    }

    private void deleteChat(String chatId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Chats").document(chatId).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("ELIMINAR CHAT","SE PUDO");
                        deleteMessageByChat(chatId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ELIMINAR CHAT","NO SE PUDO");
                    }
                });
    }

    private void deleteMessageByChat(String chatId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("ELIMINAR MENSAJES", chatId);

        // Consulta para obtener los mensajes con el chatId dado
        Query query = db.collection("Messages").whereEqualTo("idChat", chatId);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();

                    // Eliminar los documentos de Messages con el chatId dado
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot document : documents) {
                        batch.delete(document.getReference());
                    }

                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("ELIMINAR MENSAJES", "SE PUDIERON ELIMINAR");
                            } else {
                                Log.d("ELIMINAR MENSAJES", "NO SE PUDIERON ELIMINAR");
                            }
                        }
                    });
                } else {
                    Log.d("ELIMINAR MENSAJES", "ERROR AL OBTENER LOS MENSAJES");
                }
            }
        });
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_chats, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername;
        TextView textViewLastMessage;
        ImageView imageViewViewed;
        TextView textViewDateLastMessage;
        TextView textViewMessageNotRead;
        CircleImageView circleProfileIcon;
        FrameLayout mFrameLayoutMessageNotRead;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            textViewUsername = view.findViewById(R.id.textViewUsernameChat);
            textViewLastMessage = view.findViewById(R.id.textViewLastMessageChat);
            imageViewViewed = view.findViewById(R.id.imageViewViewedMessage);
            textViewDateLastMessage = view.findViewById(R.id.textViewDateLastMessage);
            textViewMessageNotRead = view.findViewById(R.id.textViewMessageNotRead);
            circleProfileIcon = view.findViewById(R.id.circleProfileIcon);
            mFrameLayoutMessageNotRead = view.findViewById(R.id.frameLayoutMessageNotRead);
            viewHolder = view;
        }
    }

}