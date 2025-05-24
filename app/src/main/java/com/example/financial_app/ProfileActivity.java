package com.example.financial_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.financial_app.model.UtilisateurDTO;
import com.example.financial_app.model.UtilisateurUpdateRequest;
import com.example.financial_app.network.ApiService;
import com.example.financial_app.network.RetrofitClient;
import com.example.financial_app.util.SharedPreferencesManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.ProgressBar;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextInputLayout tilNom, tilUsername, tilEmail, tilMotDePasse;
    private TextInputEditText etNom, etUsername, etEmail, etMotDePasse;
    private Button btnSave, btnCancel;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private SharedPreferencesManager prefsManager;
    private ApiService apiService;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        RetrofitClient.setContext(getApplicationContext());
        prefsManager = SharedPreferencesManager.getInstance(this);

        if (!prefsManager.isLoggedIn()) {
            finish();
            return;
        }

        userId = prefsManager.getUserId();
        if (userId == null) {
            Log.e(TAG, "User ID is null");
            Toast.makeText(this, "Erreur: ID utilisateur non trouvé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getClient().create(ApiService.class);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        initViews();
        loadUserProfile();
        setupClickListeners();
    }

    private void initViews() {
        tilNom = findViewById(R.id.tilNom);
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilMotDePasse = findViewById(R.id.tilMotDePasse);

        etNom = findViewById(R.id.etNom);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etMotDePasse = findViewById(R.id.etMotDePasse);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);

        progressBar = findViewById(R.id.progressBar);
    }

    private void loadUserProfile() {
        showLoading(true);
        Log.d(TAG, "Loading profile for user ID: " + userId);

        apiService.getUserById(userId).enqueue(new Callback<UtilisateurDTO>() {
            @Override
            public void onResponse(Call<UtilisateurDTO> call, Response<UtilisateurDTO> response) {
                showLoading(false);
                Log.d(TAG, "Get user response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    UtilisateurDTO user = response.body();
                    Log.d(TAG, "User loaded successfully: " + user.getUsername());
                    populateFields(user);
                } else {
                    Log.e(TAG, "Error loading profile: " + response.code());
                    String errorMessage = "Erreur lors du chargement du profil";

                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }

                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UtilisateurDTO> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error loading profile", t);
                Toast.makeText(ProfileActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFields(UtilisateurDTO user) {
        Log.d(TAG, "Populating fields with user data");
        etNom.setText(user.getNom() != null ? user.getNom() : "");
        etUsername.setText(user.getUsername() != null ? user.getUsername() : "");
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        etMotDePasse.setText("");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            Log.d(TAG, "Cancel button clicked");
            finish();
        });

        btnSave.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            if (validateFields()) {
                updateProfile();
            }
        });
    }

    private boolean validateFields() {
        Log.d(TAG, "Validating fields");
        boolean isValid = true;

        String nom = etNom.getText().toString().trim();
        if (nom.isEmpty()) {
            tilNom.setError("Le nom est requis");
            isValid = false;
        } else if (nom.length() < 2) {
            tilNom.setError("Le nom doit contenir au moins 2 caractères");
            isValid = false;
        } else {
            tilNom.setError(null);
        }

        String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            tilUsername.setError("Le nom d'utilisateur est requis");
            isValid = false;
        } else if (username.length() < 3) {
            tilUsername.setError("Le nom d'utilisateur doit contenir au moins 3 caractères");
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            tilEmail.setError("L'email est requis");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Format d'email invalide");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        String motDePasse = etMotDePasse.getText().toString().trim();
        if (!motDePasse.isEmpty() && motDePasse.length() < 6) {
            tilMotDePasse.setError("Le mot de passe doit contenir au moins 6 caractères");
            isValid = false;
        } else {
            tilMotDePasse.setError(null);
        }

        Log.d(TAG, "Validation result: " + isValid);
        return isValid;
    }

    private void updateProfile() {
        showLoading(true);
        Log.d(TAG, "Starting profile update for user ID: " + userId);

        String nom = etNom.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String motDePasse = etMotDePasse.getText().toString().trim();

        UtilisateurUpdateRequest updateRequest = new UtilisateurUpdateRequest();
        updateRequest.setNom(nom);
        updateRequest.setUsername(username);
        updateRequest.setEmail(email);

        if (!motDePasse.isEmpty()) {
            updateRequest.setMotDePasse(motDePasse);
            Log.d(TAG, "Password will be updated");
        } else {
            Log.d(TAG, "Password will not be updated (empty)");
        }

        Log.d(TAG, "Update request: nom=" + nom + ", username=" + username + ", email=" + email);

        apiService.updateUser(userId, updateRequest).enqueue(new Callback<UtilisateurDTO>() {
            @Override
            public void onResponse(Call<UtilisateurDTO> call, Response<UtilisateurDTO> response) {
                showLoading(false);
                Log.d(TAG, "Update response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    UtilisateurDTO updatedUser = response.body();
                    Log.d(TAG, "Profile updated successfully");

                    prefsManager.saveUserInfo(
                            updatedUser.getId(),
                            updatedUser.getUsername(),
                            updatedUser.getEmail(),
                            updatedUser.getNom()
                    );

                    Toast.makeText(ProfileActivity.this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Error updating profile: " + response.code());
                    String errorMessage = "Erreur lors de la mise à jour";

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);

                            switch (response.code()) {
                                case 400:
                                    errorMessage = "Données invalides. Vérifiez vos informations.";
                                    break;
                                case 409:
                                    errorMessage = "Le nom d'utilisateur ou l'email est déjà utilisé.";
                                    break;
                                case 401:
                                    errorMessage = "Session expirée. Veuillez vous reconnecter.";
                                    break;
                                case 404:
                                    errorMessage = "Utilisateur non trouvé.";
                                    break;
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }

                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UtilisateurDTO> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error updating profile", t);
                Toast.makeText(ProfileActivity.this,
                        "Erreur réseau. Vérifiez votre connexion: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        Log.d(TAG, "Show loading: " + show);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
        btnCancel.setEnabled(!show);

        etNom.setEnabled(!show);
        etUsername.setEnabled(!show);
        etEmail.setEnabled(!show);
        etMotDePasse.setEnabled(!show);
    }
}