package com.example.financial_app.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.financial_app.LoginActivity;
import com.example.financial_app.util.SharedPreferencesManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.time.LocalDateTime;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static Retrofit retrofit;
    private static String baseUrl = "http://192.168.1.128:8080/api/";
    private static Context context;

    public static void setBaseUrl(String newBaseUrl) {
        baseUrl = newBaseUrl;
        retrofit = null;
    }

    public static void setContext(Context appContext) {
        context = appContext;
    }

    public static Retrofit getClient() {
        return getClient(baseUrl);
    }

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            // Intercepteur d'authentification amélioré
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                // Debug: Afficher l'URL de la requête
                Log.d(TAG, "URL de la requête: " + original.url());

                // Récupérer le token depuis SharedPreferencesManager
                if (context != null) {
                    SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(context);
                    String token = prefsManager.getToken();
                    Log.d(TAG, "Token récupéré: "
                            + (token.isEmpty() ? "VIDE" : "PRÉSENT (" + token.length() + " caractères)"));

                    if (!token.isEmpty()) {
                        String authHeader = "Bearer " + token;
                        requestBuilder.addHeader("Authorization", authHeader);
                        Log.d(TAG, "Header Authorization ajouté: "
                                + authHeader.substring(0, Math.min(20, authHeader.length())) + "...");
                    } else {
                        Log.w(TAG, "ATTENTION: Aucun token disponible pour l'authentification");
                    }
                } else {
                    Log.e(TAG, "ERREUR: Context est null, impossible de récupérer le token");
                }

                Request request = requestBuilder.build();

                // Debug: Afficher tous les headers
                Log.d(TAG, "Headers de la requête:");
                request.headers().names().forEach(name -> {
                    Log.d(TAG, "  " + name + ": " + request.header(name));
                });

                // Exécuter la requête et capturer la réponse
                Response response = chain.proceed(request);
                Log.d(TAG, "Code de réponse: " + response.code());

                if (response.code() == 401) {
                    Log.e(TAG, "ERREUR 401: Non autorisé - Problème d'authentification détecté");
                    // Effacer la session et rediriger vers l'écran de connexion
                    if (context != null) {
                        SharedPreferencesManager.getInstance(context).clearSession();
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    }
                } else if (response.code() == 403) {
                    Log.e(TAG, "ERREUR 403: Accès interdit - Token valide mais pas les bonnes permissions");
                }

                return response;
            });

            // Création d'un Gson personnalisé avec l'adaptateur pour LocalDateTime
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}