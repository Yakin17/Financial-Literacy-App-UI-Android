package com.example.financial_app.service;

import android.content.Context;
import android.util.Log;

import com.example.financial_app.model.ScoreResponse;
import com.example.financial_app.network.ApiService;
import com.example.financial_app.network.RetrofitClient;
import com.example.financial_app.util.SharedPreferencesManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoreService {
    private static final String TAG = "ScoreService";

    private Context context;
    private SharedPreferencesManager prefsManager;
    private ApiService apiService;

    public interface ScoreCallback {
        void onSuccess(ScoreResponse response);

        void onError(String message);
    }

    public interface CheckCompletionCallback {
        void onComplete(boolean isCompleted, int previousScore);

        void onError(String message);
    }

    public ScoreService(Context context) {
        this.context = context;
        this.prefsManager = SharedPreferencesManager.getInstance(context);
        this.apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    public void checkQuizCompletion(Long quizId, CheckCompletionCallback callback) {
        Log.d(TAG, "Vérification quiz " + quizId + " - Début de la vérification");

        // Si l'utilisateur n'est pas connecté, on ne peut pas vérifier
        if (!prefsManager.isLoggedIn()) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        Long userId = prefsManager.getUserId();
        Log.d(TAG, "Vérification quiz " + quizId + " - UserID: " + userId);

        // Vérification locale en premier pour éviter un appel réseau inutile
        if (prefsManager.hasCompletedQuiz(quizId)) {
            int previousScore = prefsManager.getQuizScore(quizId);
            Log.d(TAG, "Quiz déjà complété localement. Score: " + previousScore);
            callback.onComplete(true, previousScore);
            return;
        }

        Log.d(TAG, "Vérification quiz " + quizId + " - Non trouvé localement, vérification serveur...");

        // Vérification avec le serveur - utiliser la méthode disponible dans
        // l'ApiService
        Call<Map<String, Boolean>> call = apiService.checkQuizCompletion(userId, quizId);
        call.enqueue(new Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Boolean> responseMap = response.body();
                        boolean completed = false;

                        // Extraire les valeurs de la réponse
                        if (responseMap.containsKey("completed")) {
                            completed = responseMap.get("completed");
                        }

                        Log.d(TAG, "Réponse du serveur: completed=" + completed);

                        if (completed) {
                            // Pour un quiz complété, on utilise 1 comme score par défaut
                            // car l'API actuelle ne renvoie pas le score
                            int defaultScore = 1;
                            // Sauvegarder localement pour les prochaines vérifications
                            prefsManager.markQuizAsCompleted(quizId);
                            prefsManager.saveQuizScore(quizId, defaultScore);
                            callback.onComplete(true, defaultScore);
                        } else {
                            callback.onComplete(false, 0);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du traitement de la réponse", e);
                        callback.onError("Erreur lors du traitement de la réponse: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Erreur lors de la vérification du quiz: " + response.code());
                    callback.onError("Erreur serveur: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                String errorMsg = "Erreur réseau: " + t.getMessage();
                Log.e(TAG, "Vérification quiz " + quizId + " - " + errorMsg, t);
                callback.onError(errorMsg);
            }
        });
    }

    public void saveQuizScore(Long quizId, int score, int totalQuestions, ScoreCallback callback) {
        Log.d(TAG, "===== Début sauvegarde score =====");
        if (!prefsManager.isLoggedIn()) {
            callback.onError("Utilisateur non connecté");
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            Log.e(TAG, "Erreur sauvegarde score: ID utilisateur invalide: " + userId);
            callback.onError("ID utilisateur invalide (" + userId + ")");
            return;
        }

        if (quizId == null || quizId <= 0) {
            Log.e(TAG, "Erreur sauvegarde score: ID quiz invalide: " + quizId);
            callback.onError("ID quiz invalide");
            return;
        }

        Log.d(TAG, "Sauvegarde du score - UserId: " + userId + ", QuizId: " + quizId + ", Score: " + score);

        Call<ScoreResponse> call = apiService.submitQuizScore(userId, quizId, score);
        call.enqueue(new Callback<ScoreResponse>() {
            @Override
            public void onResponse(Call<ScoreResponse> call, Response<ScoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ScoreResponse scoreResponse = response.body();
                    Log.d(TAG, "Score sauvegardé avec succès: " + scoreResponse);
                    prefsManager.markQuizAsCompleted(quizId);
                    prefsManager.saveQuizScore(quizId, score);
                    callback.onSuccess(scoreResponse);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string()
                                : "Unknown error";
                        Log.e(TAG, "Erreur lors de la sauvegarde du score: " + response.code() + " - " + errorBody);
                        callback.onError("Erreur serveur: " + response.code());
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de la lecture de l'erreur", e);
                        callback.onError("Erreur lors de la lecture de la réponse");
                    }
                }
            }

            @Override
            public void onFailure(Call<ScoreResponse> call, Throwable t) {
                Log.e(TAG, "Erreur réseau lors de la sauvegarde du score", t);
                callback.onError("Erreur réseau: " + t.getMessage());
            }
        });
    }

    public static String formatScoreAsPercentage(int score, int total) {
        if (total <= 0)
            return "0%";
        double percentage = (double) score / total * 100;
        return String.format("%.1f%%", percentage);
    }
}