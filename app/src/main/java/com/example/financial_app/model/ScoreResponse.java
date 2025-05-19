package com.example.financial_app.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modèle pour représenter un score de quiz reçu du serveur
 */
public class ScoreResponse implements Serializable {
    private Long id;
    private Long utilisateurId;
    private Long quizId;
    private int pointsObtenus;
    private LocalDateTime datePassage;

    public ScoreResponse() {
    }

    public ScoreResponse(Long id, Long utilisateurId, Long quizId, int pointsObtenus, LocalDateTime datePassage) {
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

    public int getPointsObtenus() {
        return pointsObtenus;
    }

    public void setPointsObtenus(int pointsObtenus) {
        this.pointsObtenus = pointsObtenus;
    }

    public LocalDateTime getDatePassage() {
        return datePassage;
    }

    public void setDatePassage(LocalDateTime datePassage) {
        this.datePassage = datePassage;
    }

    @Override
    public String toString() {
        return "ScoreResponse{" +
                "id=" + id +
                ", utilisateurId=" + utilisateurId +
                ", quizId=" + quizId +
                ", pointsObtenus=" + pointsObtenus +
                ", datePassage=" + datePassage +
                '}';
    }
}