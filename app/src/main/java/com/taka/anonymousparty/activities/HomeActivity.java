package com.taka.anonymousparty.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.adapters.ChatsAdapter;
import com.taka.anonymousparty.models.Chat;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.ChatsProvider;
import com.taka.anonymousparty.providers.TokenProvider;
import com.taka.anonymousparty.providers.UsersProvider;
import com.taka.anonymousparty.providers.ChatsProvider.ChatExistsCallback;
import com.taka.anonymousparty.utils.ViewedMessageHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import dmax.dialog.SpotsDialog;


public class HomeActivity extends AppCompatActivity {
    FloatingActionButton mFab;
    Toolbar mToolbar;
    UsersProvider mUsersProvider;
    AuthProvider mAuthProvider;
    TokenProvider mTokenProvider;
    ChatsProvider mChatsProvider;
    ChatsAdapter mChatsAdapter;
    RecyclerView mRecyclerView;
    AlertDialog mDialog;

    int mIdNotificationChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mFab = findViewById(R.id.fab);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chats");;

        mRecyclerView = findViewById(R.id.recyclerViewChats);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mUsersProvider = new UsersProvider();
        mChatsProvider = new ChatsProvider();
        mAuthProvider = new AuthProvider();

        mTokenProvider = new TokenProvider();
        createToken();

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNewChat();
            }
        });

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Wait a moment")
                .setCancelable(false)
                .setTheme(R.style.CustomSpotsDialog)
                .build();

        Query query = mChatsProvider.getAll(mAuthProvider.getUid());
        FirestoreRecyclerOptions<Chat> options =
                new FirestoreRecyclerOptions.Builder<Chat>()
                        .setQuery(query, Chat.class)
                        .build();
        mChatsAdapter = new ChatsAdapter(options, this);
        mRecyclerView.setAdapter(mChatsAdapter);
        mChatsAdapter.startListening();
    }

    @Override
    protected void onStart(){
        super.onStart();
        ViewedMessageHelper.updateOnline(true, HomeActivity.this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        ViewedMessageHelper.updateOnline(false, HomeActivity.this);
    }

    protected void onDestroy(){
        super.onDestroy();
        if (mChatsAdapter.getListener() != null){
            mChatsAdapter.getListener().remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.itemLogout) {
            logout();
        } else if (item.getItemId() == R.id.itemProfile) {
            goToProfile();
        }
        return true;
    }

    private void goToProfile() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private static final int MAX_ATTEMPTS = 30;
    private int attemptCount = 0;

    private void goToNewChat() {
        mDialog.show();
        String myId = mAuthProvider.getUid();
        CollectionReference mCollectionUsers = FirebaseFirestore.getInstance().collection("Users");

        getRandomUserAndCheckChatExists(myId, mCollectionUsers);
    }

    private void getRandomUserAndCheckChatExists(String myId, CollectionReference collectionUsers) {
        if (attemptCount >= MAX_ATTEMPTS) {
            mDialog.dismiss();
            Toast.makeText(HomeActivity.this, "No users available", Toast.LENGTH_SHORT).show();
            return;
        }

        mUsersProvider.getRandomUser(collectionUsers).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String randomUserId = mUsersProvider.getRandomUserId();
                    mChatsProvider.checkIfChatExists(myId, randomUserId, new ChatExistsCallback() {
                        public void onChatExists(boolean exists) {
                            if (!exists && !myId.equals(randomUserId)) {
                                // El chat no existe y los id no son iguales
                                createChat(myId, randomUserId);
                                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                                intent.putExtra("idUser1", myId);
                                intent.putExtra("idUser2", randomUserId);
                                intent.putExtra("idChat", myId + randomUserId);
                                intent.putExtra("idNotificationChat", mIdNotificationChat);

                                mDialog.dismiss();
                                startActivity(intent);
                                return;
                            } else {
                                // El chat ya existe o los id son iguales
                                attemptCount++;
                                getRandomUserAndCheckChatExists(myId, collectionUsers);
                            }
                        }
                    });
                } else {
                    // Manejo de errores cuando ocurre un fallo
                    mDialog.dismiss();
                    Toast.makeText(HomeActivity.this, "It is not possible to create a chat at this time", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void createChat(String idUser1, String idUser2) {
        Chat chat = new Chat();
        chat.setIdUser1(idUser1);
        chat.setIdUser2(idUser2);
        chat.setWriting(false);
        chat.setTimestamp(new Date().getTime());
        chat.setIdChat(idUser1 + idUser2);
        Random random = new Random();
        mIdNotificationChat = random.nextInt(1000000);
        chat.setIdNotificationChat(mIdNotificationChat);

        ArrayList<String> idsUsers = new ArrayList<>();
        idsUsers.add(idUser1);
        idsUsers.add(idUser2);
        chat.setIdsUsers(idsUsers);
        mChatsProvider.create(chat);
    }

    private void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void createToken(){mTokenProvider.create(mAuthProvider.getUid());}

}