package com.example.financial_app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Classe utilitaire pour gérer la session utilisateur et les préférences de l'application
 */
public class SessionManager {
    private static final String TAG = "SessionManager";

    // Constantes pour SharedPreferences
    private static final String PREF_NAME = "FinancialAppPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Constructeur
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Crée une session utilisateur après une connexion réussie
     */
    public void createLoginSession(Long userId, String username, String email, String role, String token) {
        // Vérification de l'ID utilisateur
        if (userId == null || userId <= 0) {
            Log.e(TAG, "Tentative de création de session avec un ID utilisateur invalide: " + userId);
            return;
        }

        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_AUTH_TOKEN, token);

        // Commettre les changements
        editor.apply();

        Log.d(TAG, "Session utilisateur créée pour: " + username + " avec ID: " + userId);

        // Vérifier immédiatement si les données ont été correctement enregistrées
        verifySessionData();
    }

    /**
     * Vérifie si les données de session ont été correctement enregistrées
     */
    private void verifySessionData() {
        Log.d(TAG, "Vérification des données de session:");
        Log.d(TAG, "isLoggedIn: " + isLoggedIn());
        Log.d(TAG, "userId: " + getUserId());
        Log.d(TAG, "username: " + getUsername());
        Log.d(TAG, "email: " + getUserEmail());
        Log.d(TAG, "role: " + getUserRole());
    }

    /**
     * Vérifie si l'utilisateur est connecté
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     */
    public Long getUserId() {
        long userId = pref.getLong(KEY_USER_ID, -1);
        // Log pour débogage
        if (userId == -1 && isLoggedIn()) {
            Log.w(TAG, "Attention: l'utilisateur est marqué comme connecté mais aucun ID n'est trouvé");
        }
        return userId;
    }

    /**
     * Récupère le nom d'utilisateur
     */
    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    /**
     * Récupère l'email de l'utilisateur
     */
    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Récupère le rôle de l'utilisateur
     */
    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "ROLE_USER");
    }

    /**
     * Vérifie si l'utilisateur est admin
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(getUserRole());
    }

    /**
     * Récupère le token d'authentification
     */
    public String getAuthToken() {
        return pref.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Déconnecte l'utilisateur et efface les données de session
     */
    public void logout() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Session utilisateur terminée");
    }

    /**
     * Marque un quiz comme complété par l'utilisateur
     */
    public void markQuizAsCompleted(Long quizId) {
        String key = "quiz_completed_" + quizId;
        editor.putBoolean(key, true);
        editor.apply();
        Log.d(TAG, "Quiz " + quizId + " marqué comme complété");
    }

    /**
     * Vérifie si un quiz a déjà été complété par l'utilisateur
     */
    public boolean isQuizCompleted(Long quizId) {
        String key = "quiz_completed_" + quizId;
        return pref.getBoolean(key, false);
    }

    /**
     * Sauvegarde le score d'un quiz
     */
    public void saveQuizScore(Long quizId, int score) {
        String key = "quiz_score_" + quizId;
        editor.putInt(key, score);
        editor.apply();
        Log.d(TAG, "Score " + score + " sauvegardé pour le quiz " + quizId);
    }

    /**
     * Récupère le score d'un quiz
     */
    public int getQuizScore(Long quizId) {
        String key = "quiz_score_" + quizId;
        return pref.getInt(key, -1);
    }

    /**
     * Pour le débogage: affiche toutes les valeurs dans les SharedPreferences
     */
    public void debugPrintAllValues() {
        Log.d(TAG, "--- DÉBUT DEBUG SHAREDPREFERENCES ---");
        Log.d(TAG, "isLoggedIn: " + isLoggedIn());
        Log.d(TAG, "userId: " + getUserId());
        Log.d(TAG, "username: " + getUsername());
        Log.d(TAG, "email: " + getUserEmail());
        Log.d(TAG, "role: " + getUserRole());
        Log.d(TAG, "auth token exists: " + (getAuthToken() != null));
        Log.d(TAG, "--- FIN DEBUG SHAREDPREFERENCES ---");
    }
}