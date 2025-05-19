package com.example.financial_app.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Score implements Serializable {
    private Long id;
    private Long utilisateurId;
    private Long quizId;
    private Integer pointsObtenus;
    private LocalDateTime datePassage;

    public Score() {
    }

    public Score(Long id, Long utilisateurId, Long quizId, Integer pointsObtenus, LocalDateTime datePassage) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.quizId = quizId;
        this.pointsObtenus = pointsObtenus;
        this.datePassage = datePassage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Integer getPointsObtenus() {
        return pointsObtenus;
    }

    public void setPointsObtenus(Integer pointsObtenus) {
        this.pointsObtenus = pointsObtenus;
    }

    public LocalDateTime getDatePassage() {
        return datePassage;
    }

    public void setDatePassage(LocalDateTime datePassage) {
        this.datePassage = datePassage;
    }
}