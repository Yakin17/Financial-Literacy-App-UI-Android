package com.example.financial_app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class SessionManager {
    private static final String TAG = "SessionManager";

    // Shared preferences file name
    private static final String PREF_NAME = "FinancialAppLogin";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_COMPLETED_QUIZZES = "completedQuizzes";
    private static final String KEY_QUIZ_SCORES_PREFIX = "quizScore_";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Créer une session de connexion
     */
    public void createLoginSession(Long userId, String username, String email, String authToken) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_AUTH_TOKEN, authToken);

        // commit changes
        editor.apply();

        Log.d(TAG, "Session de connexion créée pour " + username + " avec ID: " + userId);
    }

    /**
     * Vérifie si l'utilisateur est connecté
     */
    public boolean isLoggedIn() {
        boolean loginFlag = pref.getBoolean(KEY_IS_LOGGED_IN, false);
        long userId = pref.getLong(KEY_USER_ID, -1);

        // On est vraiment connecté seulement si le flag est true ET qu'on a un ID utilisateur valide
        boolean reallyLoggedIn = loginFlag && userId > 0;
        if (loginFlag && userId <= 0) {
            Log.e(TAG, "Incohérence détectée: flag login=true mais userId invalide (" + userId + ")");
        }
        return reallyLoggedIn;
    }

    /**
     * Déconnecte l'utilisateur
     */
    public void logout() {
        // Ne pas effacer les quizzes complétés et scores lors de la déconnexion
        // On garde juste les informations de connexion
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();

        Log.d(TAG, "Utilisateur déconnecté");
    }

    /**
     * Marque un quiz comme complété
     */
    public void markQuizAsCompleted(Long quizId) {
        Set<String> completedQuizzes = pref.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        Set<String> updatedSet = new HashSet<>(completedQuizzes);
        updatedSet.add(String.valueOf(quizId));

        editor.putStringSet(KEY_COMPLETED_QUIZZES, updatedSet);
        editor.apply();

        Log.d(TAG, "Quiz " + quizId + " marqué comme complété");
    }

    /**
     * Vérifie si un quiz a été complété
     */
    public boolean hasCompletedQuiz(Long quizId) {
        Set<String> completedQuizzes = pref.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        return completedQuizzes.contains(String.valueOf(quizId));
    }

    /**
     * Enregistre le score d'un quiz
     */
    public void saveQuizScore(Long quizId, int score) {
        String key = KEY_QUIZ_SCORES_PREFIX + quizId;
        editor.putInt(key, score);
        editor.apply();

        Log.d(TAG, "Score " + score + " enregistré pour le quiz " + quizId);
    }

    /**
     * Récupère le score d'un quiz
     */
    public int getQuizScore(Long quizId) {
        String key = KEY_QUIZ_SCORES_PREFIX + quizId;
        return pref.getInt(key, 0);
    }

    /**
     * Getters
     */
    public Long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getAuthToken() {
        return pref.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Méthode de débogage pour afficher toutes les valeurs
     */
    public void debugPrintAllValues() {
        Log.d(TAG, "==== SESSION VALUES ====");
        Log.d(TAG, "isLoggedIn: " + isLoggedIn());
        Log.d(TAG, "userId: " + getUserId());
        Log.d(TAG, "username: " + getUsername());
        Log.d(TAG, "email: " + getEmail());
        Log.d(TAG, "authToken: " + (getAuthToken() != null ? "Set" : "Not Set"));

        // Afficher les quiz complétés
        Set<String> completedQuizzes = pref.getStringSet(KEY_COMPLETED_QUIZZES, new HashSet<>());
        Log.d(TAG, "completedQuizzes: " + completedQuizzes.toString());

        // Afficher les scores des quiz
        for (String quizId : completedQuizzes) {
            String key = KEY_QUIZ_SCORES_PREFIX + quizId;
            int score = pref.getInt(key, -1);
            Log.d(TAG, "Quiz " + quizId + " score: " + score);
        }

        Log.d(TAG, "==== END SESSION VALUES ====");
    }
}