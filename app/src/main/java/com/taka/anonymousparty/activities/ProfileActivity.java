package com.taka.anonymousparty.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.taka.anonymousparty.utils.ViewedMessageHelper;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText mTextInputUsername;
    private ImageButton mEditButton;
    private String mPreviousUsername;
    private CircleImageView mCircleImageViewBack;
    private CircleImageView mCircleImageViewProfile;
    private ImageView mImageViewCover;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Reconocimiento de objetos
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mCircleImageViewProfile = findViewById(R.id.circleImageProfile);
        mTextInputUsername = findViewById(R.id.usernameEditText);
        mEditButton = findViewById(R.id.editButton);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        // Inicialmente, deshabilitar el campo de texto y configurar el color del botón
        mTextInputUsername.setEnabled(false);
        mEditButton.setColorFilter(Color.DKGRAY);

        //Display user info
        getUserInfo();

        // Configurar el evento onClickListener para el botón de edición
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextInputUsername.isEnabled()) {
                    // User clicked the edit button while the EditText is enabled
                    // Disable the EditText and revert the username if it was empty
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

        //Botón Back
        mCircleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        ViewedMessageHelper.updateOnline(true, ProfileActivity.this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        ViewedMessageHelper.updateOnline(false, ProfileActivity.this);
    }

    private void getUserInfo(){
        mUsersProvider.getUser(mAuthProvider.getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String username = documentSnapshot.getString("username");
                    mTextInputUsername.setText(username);
                }
            }
        });
    }

    private void updateUsername(){
        String username = mTextInputUsername.getText().toString();
        String id = mAuthProvider.getUid();
        User user = new User();
        user.setId(id);
        user.setUsername(username);

        mUsersProvider.update(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    Toast.makeText(ProfileActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}