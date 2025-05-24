package com.example.financial_app.service;

import android.content.Context;
import android.util.Log;

import com.example.financial_app.model.Article;
import com.example.financial_app.network.ApiService;
import com.example.financial_app.network.RetrofitClient;
import com.example.financial_app.util.SharedPreferencesManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleService {
    private static final String TAG = "ArticleService";
    private final Context context;
    private final SharedPreferencesManager prefsManager;
    private final ApiService apiService;

    public interface ArticleCallback {
        void onSuccess(List<Article> articles);

        void onError(String message);
    }

    public ArticleService(Context context) {
        this.context = context;
        this.prefsManager = SharedPreferencesManager.getInstance(context);
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public void getAllArticles(final ArticleCallback callback) {
        Log.d(TAG, "Chargement des articles depuis l'API...");

        if (!prefsManager.isLoggedIn()) {
            Log.e(TAG, "Utilisateur non connecté");
            callback.onError("Session expirée. Veuillez vous reconnecter.");
            return;
        }

        apiService.getAllArticles().enqueue(new Callback<List<Article>>() {
            @Override
            public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Articles chargés avec succès: " + response.body().size() + " articles");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Erreur lors du chargement des articles";
                    if (response.code() == 401) {
                        errorMessage = "Session expirée. Veuillez vous reconnecter.";
                        prefsManager.clearSession();
                    } else if (response.code() == 403) {
                        errorMessage = "Accès non autorisé";
                    }
                    Log.e(TAG, "Erreur API: " + response.code() + " - " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<Article>> call, Throwable t) {
                Log.e(TAG, "Erreur réseau lors du chargement des articles", t);
                callback.onError("Erreur réseau: " + t.getMessage());
            }
        });
    }

    public void getMockArticles(final ArticleCallback callback) {
        Log.d(TAG, "Génération d'articles fictifs");
        List<Article> mockArticles = new ArrayList<>();

        // Ajouter quelques articles fictifs
        mockArticles.add(new Article(1L, "Introduction à l'épargne",
                "Contenu sur l'épargne...", LocalDateTime.now().minusDays(5),
                "John Doe", 1L, true));

        mockArticles.add(new Article(2L, "Les bases de l'investissement",
                "Contenu sur l'investissement...", LocalDateTime.now().minusDays(3),
                "Jane Smith", 2L, true));

        mockArticles.add(new Article(3L, "Comment gérer son budget",
                "Contenu sur la gestion de budget...", LocalDateTime.now().minusDays(1),
                "Robert Brown", 1L, true));

        mockArticles.add(new Article(4L, "La planification de la retraite",
                "Contenu sur la retraite...", LocalDateTime.now(),
                "Alice Johnson", 3L, true));

        // Simulation d'un délai réseau
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Renvoi de " + mockArticles.size() + " articles fictifs");
                callback.onSuccess(mockArticles);
            }
        }, 800);
    }
}