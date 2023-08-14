package com.taka.anonymousparty.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.models.User;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.UsersProvider;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private AuthProvider mAuthProvider;
    private UsersProvider mUsersProvider;

    private View mActionBarView;
    private ImageView mImageViewBack;
    private TextView mTextViewTitle;
    private TextInputEditText mTextInputUsername;
    private ImageButton mEditButton;
    private String mPreviousUsername;
    private CircleImageView mCircleProfileIcon;
    private CircleImageView mCircleImageChangePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        showCustomToolbar(R.layout.custom_back_toolbar);
        mCircleProfileIcon = findViewById(R.id.circleProfileIcon);
        mCircleImageChangePhoto = findViewById(R.id.circleImageChangePhoto);
        mTextInputUsername = findViewById(R.id.usernameEditText);
        mEditButton = findViewById(R.id.editButton);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        mTextInputUsername.setEnabled(false);
        mEditButton.setColorFilter(Color.BLACK);

        getUserInfo();

        mTextInputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) {
                    String textWithoutNewlines = s.toString().replace("\n", "");
                    if (!s.toString().equals(textWithoutNewlines)) {
                        mTextInputUsername.setText(textWithoutNewlines);
                        mTextInputUsername.setSelection(textWithoutNewlines.length());
                    }
                }
            }
        });

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextInputUsername.isEnabled()) {
                    String currentUsername = mTextInputUsername.getText().toString();
                    if (currentUsername.isEmpty()) {
                        mTextInputUsername.setText(mPreviousUsername);
                        Toast.makeText(ProfileActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        updateUsername();
                    }
                    mTextInputUsername.setEnabled(false);
                    mEditButton.setColorFilter(Color.BLACK);
                    mTextInputUsername.setTypeface(null);
                } else {
                    mPreviousUsername = mTextInputUsername.getText().toString();
                    mTextInputUsername.setEnabled(true);
                    mTextInputUsername.requestFocus();
                    mEditButton.setColorFilter(Color.LTGRAY);
                    mTextInputUsername.setTypeface(Typeface.create(mTextInputUsername.getTypeface(), Typeface.ITALIC));
                }
            }
        });

        findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mTextInputUsername.isEnabled()) {
                        String currentUsername = mTextInputUsername.getText().toString();
                        if (currentUsername.isEmpty()) {
                            mTextInputUsername.setText(mPreviousUsername);
                            Toast.makeText(ProfileActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            updateUsername();
                        }
                        mTextInputUsername.setEnabled(false);
                        mEditButton.setColorFilter(Color.BLACK);
                        mTextInputUsername.setTypeface(null);
                    }
                }
                return false;
            }
        });

        mCircleImageChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ImageGridActivity.class);
            imageGridLauncher.launch(intent);
        });

        mUsersProvider.updateOnline(true, ProfileActivity.this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        mUsersProvider.updateOnline(true, ProfileActivity.this);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mUsersProvider.updateOnline(false, ProfileActivity.this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
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
        mImageViewBack = mActionBarView.findViewById(R.id.imageViewBack);
        mTextViewTitle = mActionBarView.findViewById(R.id.textViewTitle);
        mTextViewTitle.setText("Profile");

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private final ActivityResultLauncher<Intent> imageGridLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        String imageUrl = result.getData().getStringExtra("selected_image_url");
                        Glide.with(this).load(imageUrl).into(mCircleProfileIcon);
                        updateProfileIcon(imageUrl);
                    }
                }
            }
    );

    private void getUserInfo(){
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String username = documentSnapshot.getString("username");
                    mTextInputUsername.setText(username);
                    if (documentSnapshot.contains("imageProfile")) {
                        String imageProfile = documentSnapshot.getString("imageProfile");
                        if (imageProfile != null) {
                            if (!imageProfile.equals("")) {
                                Glide.with(ProfileActivity.this).load(imageProfile).into(mCircleProfileIcon);
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

    private void updateUsername(){
        String username = mTextInputUsername.getText().toString().trim();
        String id = mAuthProvider.getUid();
        User user = new User();
        user.setId(id);
        user.setUsername(username);

        mUsersProvider.updateUsername(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(ProfileActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateProfileIcon(String imageUrl){
        String id = mAuthProvider.getUid();
        User user = new User();
        user.setId(id);
        user.setImageProfile(imageUrl);

        mUsersProvider.updateUProfileIcon(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(ProfileActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}