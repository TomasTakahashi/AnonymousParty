package com.taka.anonymousparty.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.taka.anonymousparty.R;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_SCREEN_DELAY = 500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el diseño de la pantalla de inicio personalizada si es necesario
        setContentView(R.layout.activity_splash_screen); // Cambia por el nombre de tu diseño

        // Crear un Handler para realizar el retraso
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Verificar el estado de inicio de sesión
                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
                boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

                // Determinar la actividad a iniciar según el estado de inicio de sesión
                Class<?> targetActivityClass = isLoggedIn ? HomeActivity.class : MainActivity.class;

                // Iniciar la actividad apropiada
                Intent intent = new Intent(SplashScreenActivity.this, targetActivityClass);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN_DELAY);
    }
}
