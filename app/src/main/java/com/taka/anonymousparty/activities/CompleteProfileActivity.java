package com.taka.anonymousparty.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.models.Image;
import com.taka.anonymousparty.models.User;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.UsersProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class CompleteProfileActivity extends AppCompatActivity {
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    private  CircleImageView mCircleProfileIcon;
    private CircleImageView mCircleImageChangePhoto;
    private TextInputEditText mTextInputUserName;
    private Button mButtonRegister;
    private AlertDialog mDialog;

    private String mImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        mCircleProfileIcon = findViewById(R.id.circleProfileIcon);
        mCircleImageChangePhoto = findViewById(R.id.circleImageChangePhoto);
        mTextInputUserName = findViewById(R.id.textInputUserName);
        mButtonRegister = findViewById(R.id.btnConfirm);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();


        mTextInputUserName.setText("Anónimo");

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Wait a moment")
                .setCancelable(false)
                .setTheme(R.style.CustomSpotsDialog)
                .build();

        mCircleImageChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(CompleteProfileActivity.this, ImageGridActivity.class);
            imageGridLauncher.launch(intent);
        });

        mButtonRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                register();
            }
        });

        selectRandomImage();
    }

    private final ActivityResultLauncher<Intent> imageGridLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        mImageUrl = result.getData().getStringExtra("selected_image_url");
                        Glide.with(this).load(mImageUrl).into(mCircleProfileIcon);
                    }
                    else{
                        mCircleProfileIcon.setImageResource(R.drawable.ic_person);
                    }
                }
            }
    );

    private void selectRandomImage() {
        mDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> imageUrls = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Image image = dataSnapshot.getValue(Image.class);
                    if (image != null) {
                        imageUrls.add(image.getImageURL());
                    }
                }

                if (!imageUrls.isEmpty()) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(imageUrls.size());
                    mImageUrl = imageUrls.get(randomIndex);

                    // Cargar la imagen seleccionada aleatoriamente en mCircleProfileIcon
                    Glide.with(CompleteProfileActivity.this).load(mImageUrl).into(mCircleProfileIcon);
                } else {
                    // Si no hay imágenes disponibles, puedes establecer una imagen predeterminada aquí
                    mCircleProfileIcon.setImageResource(R.drawable.ic_person);
                }

                mDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mDialog.dismiss();
                Toast.makeText(CompleteProfileActivity.this, "Error al cargar las imágenes", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void register(){
        String username = mTextInputUserName.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "To continue enter a username.", Toast.LENGTH_LONG).show();
        }
        else{
            registerUser(username);
        }
    }

    private void registerUser(String username){
        String id = mAuthProvider.getUid();
        String email = mAuthProvider.getEmail();
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);
        user.setTimestamp(new Date().getTime());
        user.setImageProfile(mImageUrl);

        mDialog.show();
        mUsersProvider.create(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mDialog.dismiss();
                if (task.isSuccessful()){
                    Intent intent = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(CompleteProfileActivity.this, "Failed to store user in database.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}