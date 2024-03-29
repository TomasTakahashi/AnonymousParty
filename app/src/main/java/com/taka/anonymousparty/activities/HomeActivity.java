package com.taka.anonymousparty.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import dmax.dialog.SpotsDialog;

public class HomeActivity extends AppCompatActivity {

    private UsersProvider mUsersProvider;
    private AuthProvider mAuthProvider;
    private TokenProvider mTokenProvider;
    private ChatsProvider mChatsProvider;
    private ChatsAdapter mChatsAdapter;
    private RecyclerView mRecyclerView;
    private AlertDialog mDialog;

    private FloatingActionButton mFab;
    private Toolbar mToolbar;

    private static final int MAX_ATTEMPTS = 30;
    private int attemptCount = 0;

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

        mUsersProvider.updateOnline(true, HomeActivity.this);
    }

    @Override
    protected void onStart(){
        super.onStart();

        mUsersProvider.updateOnline(true, HomeActivity.this);

        Query query = mChatsProvider.getAll(mAuthProvider.getUid());
        FirestoreRecyclerOptions<Chat> options =
                new FirestoreRecyclerOptions.Builder<Chat>()
                        .setQuery(query, Chat.class)
                        .build();
        mChatsAdapter = new ChatsAdapter(options, HomeActivity.this);
        mRecyclerView.setAdapter(mChatsAdapter);
        mChatsAdapter.startListening();
    }

    @Override
    public void onResume(){
        super.onResume();
        mUsersProvider.updateOnline(true, HomeActivity.this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mUsersProvider.updateOnline(false, HomeActivity.this);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mUsersProvider.updateOnline(false, HomeActivity.this);
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
        if (item.getItemId() == R.id.itemProfile) {
            goToProfile();
        }
//        else if (item.getItemId() == R.id.itemSettings) {
//            goToSettings();
//        }
        else if (item.getItemId() == R.id.itemAbout) {
            goToAbout();
        }
        else if (item.getItemId() == R.id.itemLogout) {
            logout();
        }
        return true;
    }

    private void goToProfile() {
        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    private void goToSettings() {
        Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void goToAbout() {
        Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    private void logout() {
        mAuthProvider.logout();

        // Actualizar el estado de inicio de sesión en SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

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
                                attemptCount++;
                                getRandomUserAndCheckChatExists(myId, collectionUsers);
                            }
                        }
                    });
                } else {
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
        chat.setLastMessageTime(chat.getTimestamp());
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

    private void createToken(){mTokenProvider.create(mAuthProvider.getUid());}

}