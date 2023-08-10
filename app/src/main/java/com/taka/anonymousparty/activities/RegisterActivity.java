package com.taka.anonymousparty.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.models.Image;
import com.taka.anonymousparty.models.User;
import com.taka.anonymousparty.providers.AuthProvider;
import com.taka.anonymousparty.providers.UsersProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    private  CircleImageView mCircleProfileIcon;
    private CircleImageView mCircleImageChangePhoto;
    private String mImageUrl;
    TextInputEditText mTextInputUserName;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPassword;
    TextInputEditText mTextInputConfirmPassword;
    Button mButtonRegister;
    AuthProvider mAuthProvider;
    UsersProvider mUsersProvider;
    AlertDialog mDialog;
    CircleImageView mCircleImageViewBack;

    LinearLayout mLinearLayoutVerificationEmail;
    TextView mTextViewVerificationEmailTime;

    private CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mCircleProfileIcon = findViewById(R.id.circleProfileIcon);
        mCircleImageChangePhoto = findViewById(R.id.circleImageChangePhoto);
        mTextInputUserName = findViewById(R.id.textInputUserName);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputPassword = findViewById(R.id.textInputPassword);
        mTextInputConfirmPassword = findViewById(R.id.textInputConfirmPassword);
        mButtonRegister = findViewById(R.id.btnRegister);
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mLinearLayoutVerificationEmail = findViewById(R.id.linearLayoutVerificationEmail);
        mTextViewVerificationEmailTime = findViewById(R.id.textViewVerificationEmailTime);

        mAuthProvider = new AuthProvider();
        mUsersProvider = new UsersProvider();

        mDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Wait a moment")
                .setCancelable(false).build();

        mCircleImageChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, ImageGridActivity.class);
            imageGridLauncher.launch(intent);
        });

        mButtonRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                register();
            }
        });

        mCircleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        selectRandomImage();
    }

    @Override
    public void onStop(){
        finish();
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        finish();
        super.onDestroy();
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
                    Glide.with(RegisterActivity.this).load(mImageUrl).into(mCircleProfileIcon);
                } else {
                    // Si no hay imágenes disponibles, puedes establecer una imagen predeterminada aquí
                    mCircleProfileIcon.setImageResource(R.drawable.ic_person);
                }

                mDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                mDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Error al cargar las imágenes", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void register(){
        String username = mTextInputUserName.getText().toString().trim();
        String email = mTextInputEmail.getText().toString();
        String password = mTextInputPassword.getText().toString();
        String confirmPassword = mTextInputConfirmPassword.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Para continuar complete todos los campos", Toast.LENGTH_LONG).show();
        }
        else if (!isEmailValid(email)){
            Toast.makeText(this, "El email ingresado no es correcto", Toast.LENGTH_LONG).show();
        }
        else if (!password.equals(confirmPassword)){
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 6){
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
        }
        else{
            mCircleImageChangePhoto.setEnabled(false);
            mTextInputUserName.setEnabled(false);
            mTextInputEmail.setEnabled(false);
            mTextInputPassword.setEnabled(false);
            mTextInputConfirmPassword.setEnabled(false);
            mButtonRegister.setEnabled(false);
            mCircleImageViewBack.setEnabled(false);
            mLinearLayoutVerificationEmail.setVisibility(View.VISIBLE);

            // Registra al usuario pero no completa el registro
            registerUser(username, email, password);
        }
    }

    private void registerUser(String username, String email, String password){
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // Envía el correo electrónico de verificación
                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Inicia el temporizador de 120 segundos
                                startTimer(username, email);
                            } else {
                                // Maneja el error al enviar el correo electrónico de verificación
                                Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startTimer(String username, String email) {

        mCountDownTimer = new CountDownTimer(120000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Actualiza el TextView con el tiempo restante
                mTextViewVerificationEmailTime.setText("" + millisUntilFinished / 1000);
                FirebaseAuth.getInstance().getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                            // El usuario se ha verificado, completa el registro
                            Log.d("CREATEAUTHSTATELISTENER", "VERIFICADO");
                            completeRegistration(username, email);
                        }
                    }
                });
            }

            public void onFinish() {
                // Verifica si el usuario se ha verificado
                FirebaseAuth.getInstance().getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                            // El usuario se ha verificado, completa el registro
                            completeRegistration(username, email);
                        } else {
                            // El usuario no se ha verificado, cancela el registro y vuelve al MainActivity
                            FirebaseAuth.getInstance().getCurrentUser().delete();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                });
            }
        }.start();
    }

    private void cancelTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    private void completeRegistration(String username, String email) {
        cancelTimer();
        String autoId = mAuthProvider.getUid();

        User newUser = new User();
        newUser.setId(autoId);
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setTimestamp(new Date().getTime());
        newUser.setImageProfile(mImageUrl);

        mUsersProvider.create(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    // Guardar el estado de inicio de sesión en SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(RegisterActivity.this, "No se pudo almacenar el usuario en la base de datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}