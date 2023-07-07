package com.taka.anonymousparty.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.activities.ChatActivity;
import com.taka.anonymousparty.models.Chat;
import com.taka.anonymousparty.models.Message;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.ChatsProvider;
import com.taka.anonymousparty.providers.MessagesProvider;
import com.taka.anonymousparty.providers.UsersProvider;
import com.taka.anonymousparty.utils.RelativeTime;

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
        String idSender = "";
        if (mAuthProvider.getUid().equals(chat.getIdUser1())){
            idSender = chat.getIdUser2();
        }
        else{
            idSender = chat.getIdUser1();
        }

        getLastMessageAndDate(chatId, holder.textViewLastMessage, holder.textViewDateLastMessage);
        getMessageNotRead(chatId, idSender, holder.textViewMessageNotRead, holder.mFrameLayoutMessageNotRead);

    }

    public ListenerRegistration getListener(){
        return mListener;
    }
    private void getLastMessageAndDate(String chatId, TextView textViewLastMessage, TextView textViewDateLastMessage) {
        mListener = mMessagesProvider.getLastMessage(chatId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            Message lastMessage = queryDocumentSnapshots.getDocuments().get(0).toObject(Message.class);
                            if (lastMessage != null) {
                                String messageText = lastMessage.getMessage();
                                Long timestamp = lastMessage.getTimestamp();

                                textViewLastMessage.setText(messageText);
                                String relativeTime = RelativeTime.timeFormatAMPM(timestamp, context);
                                textViewDateLastMessage.setText(relativeTime);
                            }
                        }
                    }
                });
    }

    private void getMessageNotRead(String chatId, String idSender, TextView textViewMessageNotRead, FrameLayout mFrameLayoutMessageNotRead) {
        mListener = mMessagesProvider.getMessagesByChatAndSender(chatId, idSender).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        holder.textViewUsername.setText(username.toUpperCase());
                    }
                    //if (documentSnapshot.contains("image_profile")) {
                        //String imageProfile = documentSnapshot.getString("image_profile");
                        //if (imageProfile != null) {
                            //if (!imageProfile.isEmpty()) {
                                //Picasso.get().load(imageProfile).into(holder.circleImageChat);
                                //Picasso.with(context).load(imageProfile).into(holder.circleImageChat);
                            //}
                        //}
                    //}
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
        TextView textViewDateLastMessage;
        TextView textViewMessageNotRead;
        CircleImageView circleImageChat;
        FrameLayout mFrameLayoutMessageNotRead;
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            textViewUsername = view.findViewById(R.id.textViewUsernameChat);
            textViewLastMessage = view.findViewById(R.id.textViewLastMessageChat);
            textViewDateLastMessage = view.findViewById(R.id.textViewDateLastMessage);
            textViewMessageNotRead = view.findViewById(R.id.textViewMessageNotRead);
            circleImageChat = view.findViewById(R.id.circleImageChat);
            mFrameLayoutMessageNotRead = view.findViewById(R.id.frameLayoutMessageNotRead);
            viewHolder = view;
        }
    }

}
