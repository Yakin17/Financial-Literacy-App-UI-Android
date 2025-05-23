package com.example.financial_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.financial_app.util.SharedPreferencesManager;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DURATION = 4000; // 4 secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate démarré");

        try {
            setContentView(R.layout.activity_splash);
            Log.d(TAG, "Layout chargé avec succès");

            // Rediriger vers l'activité appropriée après le délai
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d(TAG, "Démarrage de la redirection");
                        SharedPreferencesManager prefsManager = SharedPreferencesManager
                                .getInstance(SplashActivity.this);

                        Intent intent;
                        if (prefsManager.isLoggedIn()) {
                            Log.d(TAG, "Utilisateur connecté, redirection vers MainActivity");
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        } else {
                            Log.d(TAG, "Utilisateur non connecté, redirection vers LoginActivity");
                            intent = new Intent(SplashActivity.this, LoginActivity.class);
                        }

                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la redirection: " + e.getMessage(), e);
                        Toast.makeText(SplashActivity.this,
                                "Erreur lors du démarrage de l'application",
                                Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }, SPLASH_DURATION);
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur lors du démarrage de l'application", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SplashActivity détruite");
    }
}