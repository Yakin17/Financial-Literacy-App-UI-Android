package com.example.financial_app.model;

public class ScoreRequest {
    private Long quizId;
    private int score;

    public ScoreRequest(Long quizId, int score) {
        this.quizId = quizId;
        this.score = score;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}