package com.taka.anonymousparty.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.taka.anonymousparty.R;
import com.taka.anonymousparty.providers.UsersProvider;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_SCREEN_DELAY = 500;
    UsersProvider mUsersProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el diseño de la pantalla de inicio personalizada si es necesario
        setContentView(R.layout.activity_splash_screen); // Cambia por el nombre de tu diseño

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_splash_screen);
        ImageView imageView = findViewById(R.id.imageIcon);
        imageView.startAnimation(animation);

        // Crear un Handler para realizar el retraso
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userId = currentUser.getUid();

                mUsersProvider = new UsersProvider();
                mUsersProvider.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            Log.d("-------SPLASH SCREEN ACTIVITY-------", "EL USUARIO EXISTE");
                            // Determinar la actividad a iniciar según el estado de inicio de sesión

                            SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                            boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                            Class<?> targetActivityClass = isLoggedIn ? HomeActivity.class : MainActivity.class;

                            // Iniciar la actividad apropiada
                            Intent intent = new Intent(SplashScreenActivity.this, targetActivityClass);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", false);
                            editor.apply();

                            Log.d("-------SPLASH SCREEN ACTIVITY-------", "EL USUARIO NO EXISTE");

                            // Iniciar la actividad apropiada
                            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

            }
        }, SPLASH_SCREEN_DELAY);
    }

}
