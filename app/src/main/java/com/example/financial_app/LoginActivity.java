package com.example.financial_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.financial_app.model.LoginRequest;
import com.example.financial_app.model.LoginResponse;
import com.example.financial_app.network.AuthService;
import com.example.financial_app.network.RetrofitClient;
import com.example.financial_app.util.SharedPreferencesManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, serverUrlEditText;
    private Button loginButton, saveUrlButton;
    private TextView registerLink;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialiser le contexte pour RetrofitClient
        RetrofitClient.setContext(getApplicationContext());

        // Initialiser SharedPreferencesManager
        prefsManager = SharedPreferencesManager.getInstance(this);

        if (prefsManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        serverUrlEditText = findViewById(R.id.editTextServerUrl);
        loginButton = findViewById(R.id.buttonLogin);
        saveUrlButton = findViewById(R.id.buttonSaveUrl);
        registerLink = findViewById(R.id.textViewRegister);

        String savedUrl = prefsManager.getServerUrl();
        serverUrlEditText.setText(savedUrl);
        RetrofitClient.setBaseUrl(savedUrl);

        saveUrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUrl = serverUrlEditText.getText().toString().trim();
                if (!newUrl.isEmpty() && newUrl.startsWith("http")) {
                    // Ensure URL ends with /api/
                    if (!newUrl.endsWith("/api/")) {
                        newUrl = newUrl + "/api/";
                    }

                    prefsManager.saveServerUrl(newUrl);
                    RetrofitClient.setBaseUrl(newUrl);

                    Toast.makeText(LoginActivity.this, "URL du serveur mis à jour", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "URL invalide", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(email, password);

        AuthService authService = RetrofitClient.getClient().create(AuthService.class);

        authService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save user details using SharedPreferencesManager
                    String token = response.body().getToken();
                    Log.d("LoginActivity", "Token reçu du serveur: " + (token != null ? "présent" : "absent"));
                    
                    prefsManager.saveToken(token);
                    prefsManager.saveUserId(response.body().getId());
                    prefsManager.saveUsername(response.body().getUsername());
                    prefsManager.saveEmail(response.body().getEmail());
                    prefsManager.saveRole(response.body().getRole());

                    // Vérifier que le token a bien été sauvegardé
                    String savedToken = prefsManager.getToken();
                    Log.d("LoginActivity", "Token sauvegardé dans les préférences: " + (savedToken.isEmpty() ? "absent" : "présent"));

                    goToMainActivity();
                } else {
                    String errorMessage = "Échec de la connexion";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Handle network error
                Toast.makeText(LoginActivity.this, "Erreur de connexion : " + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}