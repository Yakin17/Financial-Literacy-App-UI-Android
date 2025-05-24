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
    private static final String KEY_NOM = "nom";

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
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Tentative de sauvegarde d'un token vide ou null");
            return;
        }
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
        Log.d(TAG, "Token sauvegardé: " + token.substring(0, Math.min(20, token.length())) + "...");
    }

    public String getToken() {
        String token = sharedPreferences.getString(KEY_TOKEN, "");
        Log.d(TAG, "Token récupéré: " + (token.isEmpty() ? "VIDE" : "PRÉSENT (" + token.length() + " caractères)"));
        return token;
    }

    public void saveUserId(Long userId) {
        if (userId == null || userId <= 0) {
            Log.e(TAG, "Tentative de sauvegarde d'un userId invalide: " + userId);
            return;
        }
        sharedPreferences.edit().putLong(KEY_USER_ID, userId).apply();
        Log.d(TAG, "UserId sauvegardé: " + userId);
    }

    public Long getUserId() {
        Long userId = sharedPreferences.getLong(KEY_USER_ID, -1);
        Log.d(TAG, "UserId récupéré: " + userId);
        return userId;
    }

    public void saveUsername(String username) {
        if (username == null || username.isEmpty()) {
            Log.e(TAG, "Tentative de sauvegarde d'un username vide ou null");
            return;
        }
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
        Log.d(TAG, "Username sauvegardé: " + username);
    }

    public String getUsername() {
        String username = sharedPreferences.getString(KEY_USERNAME, "");
        Log.d(TAG, "Username récupéré: " + (username.isEmpty() ? "VIDE" : username));
        return username;
    }

    public void saveEmail(String email) {
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Tentative de sauvegarde d'un email vide ou null");
            return;
        }
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply();
        Log.d(TAG, "Email sauvegardé: " + email);
    }

    public String getEmail() {
        String email = sharedPreferences.getString(KEY_EMAIL, "");
        Log.d(TAG, "Email récupéré: " + (email.isEmpty() ? "VIDE" : email));
        return email;
    }

    public void saveNom(String nom) {
        if (nom == null || nom.isEmpty()) {
            Log.e(TAG, "Tentative de sauvegarde d'un nom vide ou null");
            return;
        }
        sharedPreferences.edit().putString(KEY_NOM, nom).apply();
        Log.d(TAG, "Nom sauvegardé: " + nom);
    }

    public String getNom() {
        String nom = sharedPreferences.getString(KEY_NOM, "");
        Log.d(TAG, "Nom récupéré: " + (nom.isEmpty() ? "VIDE" : nom));
        return nom;
    }

    public void saveRole(String role) {
        if (role == null || role.isEmpty()) {
            Log.e(TAG, "Tentative de sauvegarde d'un rôle vide ou null");
            return;
        }
        sharedPreferences.edit().putString(KEY_ROLE, role).apply();
        Log.d(TAG, "Rôle sauvegardé: " + role);
    }

    public String getRole() {
        String role = sharedPreferences.getString(KEY_ROLE, "");
        Log.d(TAG, "Rôle récupéré: " + (role.isEmpty() ? "VIDE" : role));
        return role;
    }

    public void saveServerUrl(String url) {
        if (url == null || url.isEmpty()) {
            Log.e(TAG, "Tentative de sauvegarde d'une URL vide ou null");
            return;
        }
        sharedPreferences.edit().putString(KEY_SERVER_URL, url).apply();
        Log.d(TAG, "URL serveur sauvegardée: " + url);
    }

    public String getServerUrl() {
        String url = sharedPreferences.getString(KEY_SERVER_URL, "http://192.168.1.128:8080/api/");
        Log.d(TAG, "URL serveur récupérée: " + url);
        return url;
    }

    public void saveUserInfo(Long userId, String username, String email, String nom) {
        Log.d(TAG, "Sauvegarde des informations utilisateur - userId: " + userId + ", username: " + username);
        saveUserId(userId);
        saveUsername(username);
        saveEmail(email);
        saveNom(nom);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        Long userId = getUserId();
        boolean isLoggedIn = !token.isEmpty() && userId > 0;

        Log.d(TAG, "Vérification connexion - Token: " + (!token.isEmpty() ? "PRÉSENT" : "ABSENT") +
                ", UserId: " + userId + ", Résultat: " + isLoggedIn);

        return isLoggedIn;
    }

    public void clearSession() {
        Log.d(TAG, "Effacement de la session");
        sharedPreferences.edit().clear().apply();
        Log.d(TAG, "Session effacée avec succès");
    }

    public void markQuizAsCompleted(Long quizId) {
        if (quizId == null || quizId <= 0) {
            Log.e(TAG, "Tentative de marquer un quiz invalide comme complété: " + quizId);
            return;
        }
        Set<String> completedQuizzes = sharedPreferences.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        Set<String> updatedSet = new HashSet<>(completedQuizzes);
        updatedSet.add(String.valueOf(quizId));

        sharedPreferences.edit().putStringSet(KEY_COMPLETED_QUIZZES, updatedSet).apply();
        Log.d(TAG, "Quiz marqué comme complété: " + quizId);
    }

    public boolean hasCompletedQuiz(Long quizId) {
        if (quizId == null || quizId <= 0) {
            Log.e(TAG, "Tentative de vérifier un quiz invalide: " + quizId);
            return false;
        }
        Set<String> completedQuizzes = sharedPreferences.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        boolean hasCompleted = completedQuizzes.contains(String.valueOf(quizId));
        Log.d(TAG, "Vérification quiz complété " + quizId + ": " + hasCompleted);
        return hasCompleted;
    }

    public void saveQuizScore(Long quizId, int score) {
        if (quizId == null || quizId <= 0) {
            Log.e(TAG, "Tentative de sauvegarder un score pour un quiz invalide: " + quizId);
            return;
        }
        String key = KEY_QUIZ_SCORES_PREFIX + quizId;
        sharedPreferences.edit().putInt(key, score).apply();
        Log.d(TAG, "Score sauvegardé pour le quiz " + quizId + ": " + score);
    }

    public int getQuizScore(Long quizId) {
        if (quizId == null || quizId <= 0) {
            Log.e(TAG, "Tentative de récupérer un score pour un quiz invalide: " + quizId);
            return 0;
        }
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
        Log.d(TAG, "nom: " + getNom());
        Log.d(TAG, "token: " + (getToken().isEmpty() ? "Not Set" : "Set (" + getToken().length() + " chars)"));
        Log.d(TAG, "role: " + getRole());
        Log.d(TAG, "serverUrl: " + getServerUrl());

        Set<String> completedQuizzes = sharedPreferences.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        Log.d(TAG, "completedQuizzes: " + completedQuizzes.toString());

        for (String quizId : completedQuizzes) {
            String key = KEY_QUIZ_SCORES_PREFIX + quizId;
            int score = sharedPreferences.getInt(key, -1);
            Log.d(TAG, "Quiz " + quizId + " score: " + score);
        }

        Log.d(TAG, "==== END SESSION VALUES ====");
    }
}