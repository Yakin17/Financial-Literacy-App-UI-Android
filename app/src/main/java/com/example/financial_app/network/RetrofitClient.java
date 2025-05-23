package com.example.financial_app.network;

import android.content.Context;
import com.example.financial_app.util.SharedPreferencesManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.time.LocalDateTime;

public class RetrofitClient {

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

            // Ajouter l'intercepteur d'authentification
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();

                // Récupérer le token depuis SharedPreferencesManager
                if (context != null) {
                    String token = SharedPreferencesManager.getInstance(context).getToken();
                    if (!token.isEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer " + token);
                    }
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
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