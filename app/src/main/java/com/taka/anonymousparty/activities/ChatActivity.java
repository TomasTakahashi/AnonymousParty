package com.taka.anonymousparty.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.adapters.MessagesAdapter;
import com.taka.anonymousparty.models.FCMBody;
import com.taka.anonymousparty.models.FCMResponse;
import com.taka.anonymousparty.models.Message;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.ChatsProvider;
import com.taka.anonymousparty.providers.MessagesProvider;
import com.taka.anonymousparty.providers.NotificationProvider;
import com.taka.anonymousparty.providers.TokenProvider;
import com.taka.anonymousparty.providers.UsersProvider;
import com.taka.anonymousparty.utils.RelativeTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private String mExtraIdUser1;
    private String mExtraIdUser2;
    private String mExtraIdChat;
    int mIdNotificationChat;

    private AuthProvider mAuthProvider;
    private UsersProvider mUsersProvider;
    private ChatsProvider mChatsProvider;
    private MessagesProvider mMessagesProvider;
    private NotificationProvider mNotificationProvider;
    private TokenProvider mTokenProvider;

    private MessagesAdapter mMessagesAdapter;


    private View mActionBarView;
    private EditText mEditTextMessage;
    private ImageView mImageViewSendMessage;
    private CircleImageView mCircleImageProfile;
    private TextView mTextViewUsername;
    private TextView mTextViewRelativeTime;
    private ImageView mImageViewBack;
    private RecyclerView mRecyclerViewMessage;

    private LinearLayoutManager mLinearLayoutManager;

    private ListenerRegistration mListener;

    private String mMyUsername;
    private String mUsernameReceiver;
    private String mImageReceiver = "";
    private String mImageSender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();
        mChatsProvider = new ChatsProvider();
        mMessagesProvider = new MessagesProvider();
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();

        mEditTextMessage = findViewById(R.id.editTextMessage);
        mImageViewSendMessage = findViewById(R.id.imageViewSendMessage);
        mRecyclerViewMessage = findViewById(R.id.recyclerViewMessage);

        mLinearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerViewMessage.setLayoutManager(mLinearLayoutManager);

        mExtraIdUser1 = getIntent().getStringExtra("idUser1");
        mExtraIdUser2 = getIntent().getStringExtra("idUser2");
        mExtraIdChat = getIntent().getStringExtra("idChat");
        mIdNotificationChat = getIntent().getIntExtra("idNotificationChat", -1);

        showCustomToolbar(R.layout.custom_chat_toolbar);
        getMyInfoUser();

        mEditTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int lineCount = mEditTextMessage.getLineCount();
                int maxLines = 6;
                mEditTextMessage.setMaxLines(Math.min(lineCount, maxLines));
            }
        });

        mEditTextMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Ocultar el teclado después de presionar Enter o Done
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        mEditTextMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Si pierde el foco y no hay texto, establecer una línea como máximo
                if (!hasFocus && mEditTextMessage.getText().toString().isEmpty()) {
                    mEditTextMessage.setLines(1);
                }
            }
        });

        mImageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mUsersProvider.updateOnline(true, ChatActivity.this);
    }

    @Override
    public void onStart(){
        super.onStart();
        mUsersProvider.updateOnline(true, ChatActivity.this);
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

    @Override
    protected void onPause(){
        super.onPause();
        mUsersProvider.updateOnline(false, ChatActivity.this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mListener != null){
            mListener.remove();
        }

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
                Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getUserInfo();
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

    private void getMyInfoUser() {
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mMyUsername = documentSnapshot.getString("username");
                    }
                    if (documentSnapshot.contains("imageProfile")) {
                        mImageSender = documentSnapshot.getString("imageProfile");
                    }
                }
            }
        });
    }
    private void getUserInfo() {
        String idUserInfo = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idUserInfo = mExtraIdUser2;
        }
        else {
            idUserInfo = mExtraIdUser1;
        }

        mListener = mUsersProvider.getUserRealTime(idUserInfo).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("username")) {
                        mUsernameReceiver = documentSnapshot.getString("username");
                        mTextViewUsername.setText(mUsernameReceiver);
                    }
                    if (documentSnapshot.contains("online")) {
                        boolean online = documentSnapshot.getBoolean("online");
                        if (online) {
                            mTextViewRelativeTime.setText("Online");
                        }
                        else if (documentSnapshot.contains("lastConnection")) {
                            long lastConnect = documentSnapshot.getLong("lastConnection");
                            String relativeTime = RelativeTime.getTimeAgo(lastConnect, ChatActivity.this);
                            mTextViewRelativeTime.setText(relativeTime);
                        }
                    }
                    if (documentSnapshot.contains("imageProfile")) {
                        mImageReceiver = documentSnapshot.getString("imageProfile");
                        if (mImageReceiver != null) {
                            if (!mImageReceiver.equals("")) {
                                Glide.with(ChatActivity.this).load(mImageReceiver).into(mCircleImageProfile);
                            }
                        }
                        else{
                            mCircleImageProfile.setImageResource(R.drawable.ic_person);
                        }
                    }
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
        String textMessage = mEditTextMessage.getText().toString().trim();
        if (!textMessage.isEmpty()){
            final Message message = new Message();
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
                        mEditTextMessage.clearFocus();
                        mMessagesAdapter.notifyDataSetChanged();
                        getToken(message);
                        mEditTextMessage.requestFocus();
                        mChatsProvider.updateLastMessageTime(mExtraIdChat, message.getTimestamp());
                    }
                    else{
                        Toast.makeText(ChatActivity.this, "It is not possible to send the message", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void getLastThreeMessages(final Message message, final String token) {

        mMessagesProvider.getLastThreeMessagesByChatAndSender(mExtraIdChat, mAuthProvider.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                ArrayList<Message> messageArrayList = new ArrayList<>();

                for (DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                    if (d.exists()) {
                        Message message = d.toObject(Message.class);
                        messageArrayList.add(message);
                    }
                }

                if (messageArrayList.size() == 0) {
                    messageArrayList.add(message);
                }

                Collections.reverse(messageArrayList);

                if (messageArrayList.size() == 3) {
                    Message oldestMessage = messageArrayList.get(0);
                    oldestMessage.setMessage("..."); // Cambia el mensaje más viejo por "..."
                }

                Gson gson = new Gson();
                String messages = gson.toJson(messageArrayList);

                sendNotification(token, messages, message);
            }
        });
    }

    private void sendNotification(final String token, String messages, Message message) {
        final Map<String, String> data = new HashMap<>();
        data.put("title", "New message");
        data.put("body", message.getMessage());
        data.put("idNotification", String.valueOf(mIdNotificationChat));
        data.put("messages", messages);
        data.put("usernameSender", mMyUsername.toUpperCase());
        data.put("userIdSender", message.getUserIdSender());
        data.put("usernameReceiver", mUsernameReceiver.toUpperCase());
        data.put("userIdReceiver", message.getUserIdReceiver());
        data.put("idChat", message.getIdChat());

        data.put("imageSender", mImageSender);
        data.put("imageReceiver", mImageReceiver);

        FCMBody body = new FCMBody(token, "high", "4500s", data);
        mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body() != null) {
                    if (response.body().getSuccess() == 1) {
                    }
                    else {
                    }
                }
                else {
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
                Log.d("ERROR ONFAILURE", "Failed to send notification: " + t.getMessage());
            }
        });
    }

    private void getToken(final Message message) {
        String idUser = "";
        if (mAuthProvider.getUid().equals(mExtraIdUser1)) {
            idUser = mExtraIdUser2;
        }
        else {
            idUser = mExtraIdUser1;
        }
        mTokenProvider.getToken(idUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.contains("token")) {
                        String token = documentSnapshot.getString("token");
                        getLastThreeMessages(message, token);
                    }
                }
                else {
                    Toast.makeText(ChatActivity.this, "User's notification token does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}