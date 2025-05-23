package com.example.financial_app.model;

public class UserStats {
    private int totalQuizzesCompleted;
    private double averageScore;
    private int totalScore;
    private int totalQuestions;

    public UserStats() {
    }

    public UserStats(int totalQuizzesCompleted, double averageScore, int totalScore, int totalQuestions) {
        this.totalQuizzesCompleted = totalQuizzesCompleted;
        this.averageScore = averageScore;
        this.totalScore = totalScore;
        this.totalQuestions = totalQuestions;
    }

    public int getTotalQuizzesCompleted() {
        return totalQuizzesCompleted;
    }

    public void setTotalQuizzesCompleted(int totalQuizzesCompleted) {
        this.totalQuizzesCompleted = totalQuizzesCompleted;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getFormattedAverageScore() {
        return String.format("%.1f%%", averageScore);
    }
}