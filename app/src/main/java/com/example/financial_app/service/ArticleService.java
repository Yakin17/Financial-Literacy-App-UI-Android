package com.example.financial_app.service;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.financial_app.model.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleService {
    private static final String TAG = "ArticleService";
    private static final String BASE_URL = "http://192.168.1.128:8080/api"; // Remplacez par votre URL d'API
    private RequestQueue requestQueue;
    private Context context;
    private SharedPreferences sharedPreferences;

    public ArticleService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    public interface ArticleCallback {
        void onSuccess(List<Article> articles);
        void onError(String message);
    }

    public void getAllArticles(final ArticleCallback callback) {
        String url = BASE_URL + "/articles";
        Log.d(TAG, "Tentative de récupération des articles depuis: " + url);

        // D'abord, faisons un test avec une requête String pour voir la réponse exacte
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Réponse brute reçue: " + response);

                        // Maintenant, essayons de parser la réponse en JSON
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            Log.d(TAG, "JSONArray créé avec succès, contient " + jsonArray.length() + " éléments");

                            List<Article> articles = parseArticlesFromJson(jsonArray);
                            callback.onSuccess(articles);
                        } catch (JSONException e) {
                            Log.e(TAG, "Erreur lors du parsing de la réponse JSON", e);
                            callback.onError("Format de réponse invalide: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        logVolleyError(error);
                        String errorMessage = createErrorMessage(error);
                        callback.onError(errorMessage);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return createAuthHeaders();
            }
        };

        // Configurer une politique de nouvelle tentative
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 secondes de timeout
                2,     // 2 tentatives max
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Log.d(TAG, "Ajout de la requête à la file d'attente");
        requestQueue.add(stringRequest);
    }

    private List<Article> parseArticlesFromJson(JSONArray jsonArray) throws JSONException {
        List<Article> articles = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject articleObject = jsonArray.getJSONObject(i);
            Log.d(TAG, "Parsing de l'article " + i + ": " + articleObject.toString());

            Article article = new Article();

            // Extraction des champs obligatoires
            article.setId(articleObject.getLong("id"));
            article.setTitre(articleObject.getString("titre"));
            article.setContenu(articleObject.getString("contenu"));

            // Extraction de la date avec gestion des erreurs
            try {
                String dateStr = articleObject.getString("datePublication");
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);
                article.setDatePublication(dateTime);
            } catch (DateTimeParseException e) {
                Log.e(TAG, "Erreur lors du parsing de la date, utilisation de la date actuelle", e);
                article.setDatePublication(LocalDateTime.now());
            }

            // Extraction des informations sur l'auteur
            article.setAuteurId(articleObject.getLong("auteurId"));
            article.setAuteurNom(articleObject.getString("auteurNom"));

            // Vérification des quizzes
            boolean hasQuiz = false;
            if (articleObject.has("quizzes") && !articleObject.isNull("quizzes")) {
                JSONArray quizzes = articleObject.getJSONArray("quizzes");
                hasQuiz = quizzes.length() > 0;
            }
            article.setHasQuiz(hasQuiz);

            articles.add(article);
            Log.d(TAG, "Article ajouté: ID=" + article.getId() + ", Titre=" + article.getTitre());
        }

        Log.d(TAG, "Nombre total d'articles parsés: " + articles.size());
        return articles;
    }

    private Map<String, String> createAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        String token = sharedPreferences.getString("token", "");
        Log.d(TAG, "Token d'authentification: " + (token.isEmpty() ? "absent" : "présent"));

        if (!token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }

        // Ajouter des en-têtes Accept pour garantir que le serveur renvoie du JSON
        headers.put("Accept", "application/json");
        return headers;
    }

    private void logVolleyError(VolleyError error) {
        if (error.networkResponse != null) {
            Log.e(TAG, "Erreur réseau: code=" + error.networkResponse.statusCode +
                    ", data=" + (error.networkResponse.data != null ? new String(error.networkResponse.data) : "aucune donnée"));
        } else {
            Log.e(TAG, "Erreur sans réponse réseau: " + error.toString(), error);
        }
    }

    private String createErrorMessage(VolleyError error) {
        String errorMessage = "Impossible de récupérer les articles. ";

        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case 401:
                    errorMessage += "Authentification requise.";
                    break;
                case 403:
                    errorMessage += "Accès non autorisé.";
                    break;
                case 404:
                    errorMessage += "Service introuvable.";
                    break;
                case 500:
                    errorMessage += "Erreur serveur interne.";
                    break;
                default:
                    errorMessage += "Code d'erreur: " + error.networkResponse.statusCode;
            }
        } else if (error.getMessage() != null) {
            errorMessage += error.getMessage();
        } else if (error instanceof AuthFailureError) {
            errorMessage += "Erreur d'authentification.";
        } else {
            errorMessage += "Erreur de connexion. Vérifiez votre connexion Internet.";
        }

        return errorMessage;
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