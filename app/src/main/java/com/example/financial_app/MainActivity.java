package com.example.financial_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.ImageButton;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.financial_app.network.RetrofitClient;
import com.example.financial_app.util.SharedPreferencesManager;
import android.util.Log;
import com.example.financial_app.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import java.util.List;
import com.example.financial_app.model.Score;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView textViewWelcome, textViewEmail, textViewUsername, textViewQuizStats;
    private ImageButton logoutButton, profileButton;
    private ConstraintLayout startLearningButton;
    private SharedPreferencesManager prefsManager;
    private Button buttonLogin, buttonLogout;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser le contexte pour RetrofitClient
        RetrofitClient.setContext(getApplicationContext());

        // Initialiser SharedPreferencesManager
        prefsManager = SharedPreferencesManager.getInstance(this);

        // Check if user is logged in
        if (!prefsManager.isLoggedIn()) {
            goToLoginActivity();
            return;
        }

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initialize views
        initViews();

        // Get user information
        String username = prefsManager.getUsername();
        String email = prefsManager.getEmail();
        Long userId = prefsManager.getUserId();

        // Set user information
        textViewWelcome.setText(username + "!");
        textViewEmail.setText(email);
        textViewUsername.setText(username);

        // Setup click listeners
        setupClickListeners();

        checkLoginStatus();
    }

    private void initViews() {
        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewUsername = findViewById(R.id.textViewUsername);
        logoutButton = findViewById(R.id.logoutButton);
        profileButton = findViewById(R.id.profileButton);
        startLearningButton = findViewById(R.id.startLearningButton);
        textViewQuizStats = findViewById(R.id.textViewQuizStats);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogout = findViewById(R.id.buttonLogout);
    }

    private void setupClickListeners() {
        // Start Learning button click listener
        startLearningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ArticleListActivity.class);
                startActivity(intent);
            }
        });

        // Logout button click listener
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // Profile button click listener - NOUVEAU
        if (profileButton != null) {
            profileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            });
        }

        // Login button click listener
        if (buttonLogin != null) {
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToLoginActivity();
                }
            });
        }

        // Logout button (dans le card) click listener
        if (buttonLogout != null) {
            buttonLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });
        }

        // Click listener pour le nom d'utilisateur dans la toolbar - accès au profil
        textViewUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefsManager.isLoggedIn()) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void logout() {
        // Clear user session
        prefsManager.clearSession();
        // Redirect to login
        goToLoginActivity();
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void checkLoginStatus() {
        if (prefsManager.isLoggedIn()) {
            // Récupérer les informations de l'utilisateur
            String username = prefsManager.getUsername();
            String email = prefsManager.getEmail();
            Long userId = prefsManager.getUserId();

            // Mettre à jour l'interface utilisateur
            textViewUsername.setText(username);
            textViewEmail.setText(email);

            // Récupérer les statistiques de l'utilisateur
            loadUserStats();

            // Afficher les boutons de connexion/déconnexion appropriés
            if (buttonLogin != null) buttonLogin.setVisibility(View.GONE);
            if (buttonLogout != null) buttonLogout.setVisibility(View.VISIBLE);
        } else {
            // Réinitialiser l'interface utilisateur
            textViewUsername.setText("Non connecté");
            textViewEmail.setText("");
            textViewQuizStats.setText("Quiz complétés: 0\nScore moyen: 0%");

            // Afficher les boutons de connexion/déconnexion appropriés
            if (buttonLogin != null) buttonLogin.setVisibility(View.VISIBLE);
            if (buttonLogout != null) buttonLogout.setVisibility(View.GONE);
        }
    }

    private void loadUserStats() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Log.e(TAG, "loadUserStats: User ID is null");
            return;
        }

        Log.d(TAG, "loadUserStats: Loading stats for user ID: " + userId);
        apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getUserScores(userId).enqueue(new Callback<List<Score>>() {
            @Override
            public void onResponse(Call<List<Score>> call, Response<List<Score>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Score> scores = response.body();
                    Log.d(TAG, "loadUserStats: Received " + scores.size() + " scores");

                    // Calculer les statistiques
                    int totalQuizzes = scores.size();
                    int totalScore = 0;
                    for (Score score : scores) {
                        totalScore += score.getPoints();
                    }
                    double averageScore = totalQuizzes > 0 ? (double) totalScore / totalQuizzes : 0;

                    // Mettre à jour l'interface utilisateur
                    String statsText = String.format("Quizzes completed: %d\nAverage score: %.1f%%",
                            totalQuizzes, averageScore);
                    Log.d(TAG, "loadUserStats: New stats text: " + statsText);
                    updateQuizStats(statsText);
                } else {
                    Log.e(TAG, "loadUserStats: Error response - " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "loadUserStats: Error body - " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "loadUserStats: Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Score>> call, Throwable t) {
                Log.e(TAG, "loadUserStats: Network error", t);
            }
        });
    }

    private void updateQuizStats(String statsText) {
        Log.d(TAG, "Mise à jour des statistiques affichées");
        textViewQuizStats.setText(statsText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rafraîchir les informations utilisateur au retour de ProfileActivity
        if (prefsManager.isLoggedIn()) {
            checkLoginStatus();
        }
    }
}