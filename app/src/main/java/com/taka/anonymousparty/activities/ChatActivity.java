package com.taka.anonymousparty.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.adapters.MessagesAdapter;
import com.taka.anonymousparty.models.Chat;
import com.taka.anonymousparty.models.Message;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.ChatsProvider;
import com.taka.anonymousparty.providers.MessagesProvider;
import com.taka.anonymousparty.providers.UsersProvider;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String mExtraIdUser1;
    String mExtraIdUser2;
    String mExtraIdChat;

    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    ChatsProvider mChatsProvider;
    MessagesProvider mMessagesProvider;

    MessagesAdapter mMessagesAdapter;

    EditText mEditTextMessage;
    ImageView mImageViewSendMessage;

    CircleImageView mCircleImageProfile;
    TextView mTextViewUsername;
    TextView mTextViewRelativeTime;
    ImageView mImageViewBack;
    RecyclerView mRecyclerViewMessage;

    LinearLayoutManager mLinearLayoutManager;



    View mActionBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mChatsProvider = new ChatsProvider();
        mMessagesProvider = new MessagesProvider();

        mEditTextMessage = findViewById(R.id.editTextMessage);
        mImageViewSendMessage = findViewById(R.id.imageViewSendMessage);
        mRecyclerViewMessage = findViewById(R.id.recyclerViewMessage);

        mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessage.setLayoutManager(mLinearLayoutManager);

        mExtraIdUser1 = getIntent().getStringExtra("idUser1");
        mExtraIdUser2 = getIntent().getStringExtra("idUser2");
        mExtraIdChat = getIntent().getStringExtra("idChat");

        showCustomToolbar(R.layout.custom_chat_toolbar);

        mImageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        if (mExtraIdChat != null){
            if (!mExtraIdChat.isEmpty()){
                getMessageChat();
            }
        }
        if (mMessagesAdapter != null){
            mMessagesAdapter.startListening();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        mMessagesAdapter.stopListening();
    }

    public void getMessageChat(){
        Query query = mMessagesProvider.getMessageByChat(mExtraIdChat);
        FirestoreRecyclerOptions<Message> options =
                new FirestoreRecyclerOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .build();
        mMessagesAdapter = new MessagesAdapter(options, ChatActivity.this);
        mRecyclerViewMessage.setAdapter(mMessagesAdapter);
        mMessagesAdapter.startListening();
        mMessagesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateViewed();
                int numberMessage = mMessagesAdapter.getItemCount();
                int lastMessagePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastMessagePosition == -1 || (positionStart >= (numberMessage -1) && lastMessagePosition == (positionStart - 1))) {
                    mRecyclerViewMessage.scrollToPosition(positionStart);
                }
            }
        });
    }

    private void showCustomToolbar(int resource) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mActionBarView = inflater.inflate(resource, null);
        actionBar.setCustomView(mActionBarView);
        mCircleImageProfile = mActionBarView.findViewById(R.id.circleImageProfile);
        mTextViewUsername = mActionBarView.findViewById(R.id.textViewUsername);
        mTextViewRelativeTime = mActionBarView.findViewById(R.id.textViewRelativeTime);
        mImageViewBack = mActionBarView.findViewById(R.id.imageViewBack);

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getUserInfo();
    }

    private void getUserInfo() {
        String idUserInfo = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)){
            idUserInfo = mExtraIdUser2;
        }
        else{
            idUserInfo = mExtraIdUser1;
        }
        mUsersProvider.getUser(idUserInfo).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    if (documentSnapshot.contains("username")){
                        String username = documentSnapshot.getString("username");
                        mTextViewUsername.setText(username);
                    }
//                    if (documentSnapshot.contains("image_profile")){
//                        String username = documentSnapshot.getString("image_profile");
//                        if (imageProfile != null){
//                            if (!imageProfile.equals(""){
//                                Picasso.with(ChatActivity.this).load(imageProfile).into(mCircleImageProfile);
//                            }
//                        }
//                    }
                }
            }
        });
    }

    private void updateViewed(){
        String idSender = "";

        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idSender = mExtraIdUser2;
        }
        else {
            idSender = mExtraIdUser1;
        }

        mMessagesProvider.getMessagesByChatAndSender(mExtraIdChat, idSender).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    mMessagesProvider.updateViewed(document.getId(), true);
                }
            }
        });
    }

    private void sendMessage() {
        String textMessage = mEditTextMessage.getText().toString();
        if (!textMessage.isEmpty()){
            Message message = new Message();
            if (mAuthProvider.getUid().equals(mExtraIdUser1)){
                message.setUserIdSender(mExtraIdUser1);
                message.setUserIdReceiver(mExtraIdUser2);
            }
            else {
                message.setUserIdSender(mExtraIdUser2);
                message.setUserIdReceiver(mExtraIdUser1);
            }
            message.setTimestamp(new Date().getTime());
            message.setViewed(false);
            message.setIdChat(mExtraIdChat);
            message.setMessage(textMessage);

            mMessagesProvider.create(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        mEditTextMessage.setText("");
                        mMessagesAdapter.notifyDataSetChanged();
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "It is not possible to send the message", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}