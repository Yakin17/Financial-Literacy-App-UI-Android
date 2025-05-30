package com.example.financial_app.network;

import com.example.financial_app.model.Article;
import com.example.financial_app.model.LoginRequest;
import com.example.financial_app.model.LoginResponse;
import com.example.financial_app.model.Quiz;
import com.example.financial_app.model.Score;
import com.example.financial_app.model.ScoreResponse;

import com.example.financial_app.model.UtilisateurDTO;
import com.example.financial_app.model.UtilisateurUpdateRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
        // Articles endpoints
        @GET("articles")
        Call<List<Article>> getAllArticles();

        @GET("articles/{id}")
        Call<Article> getArticleById(@Path("id") Long id);

        @POST("articles")
        Call<Article> createArticle(@Body Article article);

        @PUT("articles/{id}")
        Call<Article> updateArticle(@Path("id") Long id, @Body Article article);

        @DELETE("articles/{id}")
        Call<Void> deleteArticle(@Path("id") Long id);

        // Quiz endpoints
        @GET("quizzes")
        Call<List<Quiz>> getAllQuizzes();

        @GET("quizzes/{id}")
        Call<Quiz> getQuizById(@Path("id") Long id);

        @GET("quizzes/article/{articleId}")
        Call<List<Quiz>> getQuizzesByArticleId(@Path("articleId") Long articleId);

        @POST("quizzes")
        Call<Quiz> createQuiz(@Body Quiz quiz);

        @PUT("quizzes/{id}")
        Call<Quiz> updateQuiz(@Path("id") Long id, @Body Quiz quiz);

        @DELETE("quizzes/{id}")
        Call<Void> deleteQuiz(@Path("id") Long id);

        // Score endpoints
        @POST("scores/submit/{utilisateurId}/{quizId}/{points}")
        Call<ScoreResponse> submitQuizScore(
                        @Path("utilisateurId") Long utilisateurId,
                        @Path("quizId") Long quizId,
                        @Path("points") int points);

        @GET("scores/check/{utilisateurId}/{quizId}")
        Call<Map<String, Boolean>> checkQuizCompletion(
                        @Path("utilisateurId") Long utilisateurId,
                        @Path("quizId") Long quizId);

        @GET("scores/utilisateur/{utilisateurId}")
        Call<List<Score>> getUserScores(@Path("utilisateurId") Long utilisateurId);

        // User endpoints - CORRIGÉS (suppression du préfixe "api/" redondant)
        @GET("utilisateurs/{id}")
        Call<UtilisateurDTO> getUserById(@Path("id") Long id);

        @PUT("utilisateurs/{id}")
        Call<UtilisateurDTO> updateUser(@Path("id") Long id, @Body UtilisateurUpdateRequest updateRequest);

        @GET("utilisateurs")
        Call<List<UtilisateurDTO>> getAllUsers();

        @POST("utilisateurs")
        Call<UtilisateurDTO> createUser(@Body UtilisateurUpdateRequest userRequest);

        @DELETE("utilisateurs/{id}")
        Call<Void> deleteUser(@Path("id") Long id);
}