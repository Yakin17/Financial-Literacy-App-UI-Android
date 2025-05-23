package com.example.financial_app.model;

import com.google.gson.annotations.SerializedName;

public class Score {
    @SerializedName("id")
    private Long id;

    @SerializedName("utilisateurId")
    private Long utilisateurId;

    @SerializedName("quizId")
    private Long quizId;

    @SerializedName("points")
    private int points;

    @SerializedName("dateSoumission")
    private String dateSoumission;

    public Long getId() {
        return id;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public int getPoints() {
        return points;
    }

    public String getDateSoumission() {
        return dateSoumission;
    }
}