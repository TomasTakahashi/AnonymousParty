package com.taka.anonymousparty.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.models.User;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.UsersProvider;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText mTextInputUsername;
    private ImageButton mEditButton;
    private String mPreviousUsername;
    private CircleImageView mCircleImageViewBack;
    private CircleImageView mCircleProfileIcon;
    private CircleImageView mCircleImageChangePhoto;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Reconocimiento de objetos
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mCircleProfileIcon = findViewById(R.id.circleProfileIcon);
        mCircleImageChangePhoto = findViewById(R.id.circleImageChangePhoto);
        mTextInputUsername = findViewById(R.id.usernameEditText);
        mEditButton = findViewById(R.id.editButton);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        // Inicialmente, deshabilitar el campo de texto y configurar el color del botón
        mTextInputUsername.setEnabled(false);
        mEditButton.setColorFilter(Color.DKGRAY);

        //Display user info
        getUserInfo();

        mTextInputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Eliminar los caracteres de salto de línea del texto
                if (s != null) {
                    String textWithoutNewlines = s.toString().replace("\n", "");
                    if (!s.toString().equals(textWithoutNewlines)) {
                        mTextInputUsername.setText(textWithoutNewlines);
                        mTextInputUsername.setSelection(textWithoutNewlines.length());
                    }
                }
            }
        });

        // Configurar el evento onClickListener para el botón de edición
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextInputUsername.isEnabled()) {
                    String currentUsername = mTextInputUsername.getText().toString();
                    if (currentUsername.isEmpty()) {
                        mTextInputUsername.setText(mPreviousUsername);
                        Toast.makeText(ProfileActivity.this, "Campo vacío", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        updateUsername();
                    }
                    mTextInputUsername.setEnabled(false);
                    mEditButton.setColorFilter(Color.DKGRAY);
                    mTextInputUsername.setTypeface(null);
                } else {
                    // User clicked the edit button while the EditText is disabled
                    // Enable the EditText for editing and change the color of the button
                    mPreviousUsername = mTextInputUsername.getText().toString();
                    mTextInputUsername.setEnabled(true);
                    mTextInputUsername.requestFocus();
                    mEditButton.setColorFilter(Color.LTGRAY);
                    mTextInputUsername.setTypeface(Typeface.create(mTextInputUsername.getTypeface(), Typeface.ITALIC));
                }
            }
        });

        // Configurar el evento onTouchListener para la vista raíz de la actividad
        findViewById(android.R.id.content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Si se toca en cualquier otro lugar de la pantalla, deshabilitar el campo de texto si estaba habilitado
                    if (mTextInputUsername.isEnabled()) {
                        String currentUsername = mTextInputUsername.getText().toString();
                        if (currentUsername.isEmpty()) {
                            mTextInputUsername.setText(mPreviousUsername);
                            Toast.makeText(ProfileActivity.this, "Campo vacío", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            updateUsername();
                        }
                        mTextInputUsername.setEnabled(false);
                        mEditButton.setColorFilter(Color.DKGRAY);
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

        mCircleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
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