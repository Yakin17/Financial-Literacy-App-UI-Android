package com.example.financial_app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesManager {
    private static final String TAG = "SharedPreferencesManager";
    private static final String PREF_NAME = "FinancialAppPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_SERVER_URL = "serverUrl";
    private static final String KEY_COMPLETED_QUIZZES = "completedQuizzes";
    private static final String KEY_QUIZ_SCORES_PREFIX = "quizScore_";

    private static SharedPreferencesManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
        Log.d(TAG, "Token sauvegardé: " + (token.isEmpty() ? "vide" : "présent"));
    }

    public String getToken() {
        String token = sharedPreferences.getString(KEY_TOKEN, "");
        Log.d(TAG, "Token récupéré: " + (token.isEmpty() ? "vide" : "présent"));
        return token;
    }

    public void saveUserId(Long userId) {
        sharedPreferences.edit().putLong(KEY_USER_ID, userId).apply();
        Log.d(TAG, "UserId sauvegardé: " + userId);
    }

    public Long getUserId() {
        Long userId = sharedPreferences.getLong(KEY_USER_ID, -1);
        Log.d(TAG, "UserId récupéré: " + userId);
        return userId;
    }

    public void saveUsername(String username) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    public void saveEmail(String email) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public void saveRole(String role) {
        sharedPreferences.edit().putString(KEY_ROLE, role).apply();
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, "");
    }

    public void saveServerUrl(String url) {
        sharedPreferences.edit().putString(KEY_SERVER_URL, url).apply();
    }

    public String getServerUrl() {
        return sharedPreferences.getString(KEY_SERVER_URL, "http://192.168.1.128:8080/api/");
    }

    public boolean isLoggedIn() {
        boolean isLoggedIn = !getToken().isEmpty() && getUserId() > 0;
        Log.d(TAG, "Vérification connexion - Token: " + (!getToken().isEmpty() ? "présent" : "absent") +
                ", UserId: " + getUserId() + ", Résultat: " + isLoggedIn);
        return isLoggedIn;
    }

    public void clearSession() {
        sharedPreferences.edit().clear().apply();
        Log.d(TAG, "Session effacée");
    }

    public void markQuizAsCompleted(Long quizId) {
        Set<String> completedQuizzes = sharedPreferences.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        Set<String> updatedSet = new HashSet<>(completedQuizzes);
        updatedSet.add(String.valueOf(quizId));

        sharedPreferences.edit().putStringSet(KEY_COMPLETED_QUIZZES, updatedSet).apply();
        Log.d(TAG, "Quiz marqué comme complété: " + quizId);
    }

    public boolean hasCompletedQuiz(Long quizId) {
        Set<String> completedQuizzes = sharedPreferences.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        boolean hasCompleted = completedQuizzes.contains(String.valueOf(quizId));
        Log.d(TAG, "Vérification quiz complété " + quizId + ": " + hasCompleted);
        return hasCompleted;
    }

    public void saveQuizScore(Long quizId, int score) {
        String key = KEY_QUIZ_SCORES_PREFIX + quizId;
        sharedPreferences.edit().putInt(key, score).apply();
        Log.d(TAG, "Score sauvegardé pour le quiz " + quizId + ": " + score);
    }

    public int getQuizScore(Long quizId) {
        String key = KEY_QUIZ_SCORES_PREFIX + quizId;
        int score = sharedPreferences.getInt(key, 0);
        Log.d(TAG, "Score récupéré pour le quiz " + quizId + ": " + score);
        return score;
    }

    public void debugPrintAllValues() {
        Log.d(TAG, "==== SESSION VALUES ====");
        Log.d(TAG, "isLoggedIn: " + isLoggedIn());
        Log.d(TAG, "userId: " + getUserId());
        Log.d(TAG, "username: " + getUsername());
        Log.d(TAG, "email: " + getEmail());
        Log.d(TAG, "token: " + (getToken().isEmpty() ? "Not Set" : "Set"));

        // Afficher les quiz complétés
        Set<String> completedQuizzes = sharedPreferences.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        Log.d(TAG, "completedQuizzes: " + completedQuizzes.toString());

        // Afficher les scores des quiz
        for (String quizId : completedQuizzes) {
            String key = KEY_QUIZ_SCORES_PREFIX + quizId;
            int score = sharedPreferences.getInt(key, -1);
            Log.d(TAG, "Quiz " + quizId + " score: " + score);
        }

        Log.d(TAG, "==== END SESSION VALUES ====");
    }
}