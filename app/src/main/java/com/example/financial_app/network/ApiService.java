package com.example.financial_app.network;


import com.example.financial_app.model.Article;
import com.example.financial_app.model.Quiz;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
public interface ApiService {
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
}
