package com.example.financial_app.model;

public class Quiz {
    private Long id;
    private Long articleId;
    private String question;
    private String reponseCorrecte;
    private String reponseInc1;
    private String reponseInc2;
    private String reponseInc3;

    public Quiz() {
    }

    public Quiz(Long id, Long articleId, String question, String reponseCorrecte,
                String reponseInc1, String reponseInc2, String reponseInc3) {
        this.id = id;
        this.articleId = articleId;
        this.question = question;
        this.reponseCorrecte = reponseCorrecte;
        this.reponseInc1 = reponseInc1;
        this.reponseInc2 = reponseInc2;
        this.reponseInc3 = reponseInc3;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponseCorrecte() {
        return reponseCorrecte;
    }

    public void setReponseCorrecte(String reponseCorrecte) {
        this.reponseCorrecte = reponseCorrecte;
    }

    public String getReponseInc1() {
        return reponseInc1;
    }

    public void setReponseInc1(String reponseInc1) {
        this.reponseInc1 = reponseInc1;
    }

    public String getReponseInc2() {
        return reponseInc2;
    }

    public void setReponseInc2(String reponseInc2) {
        this.reponseInc2 = reponseInc2;
    }

    public String getReponseInc3() {
        return reponseInc3;
    }

    public void setReponseInc3(String reponseInc3) {
        this.reponseInc3 = reponseInc3;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", articleId=" + articleId +
                ", question='" + question + '\'' +
                '}';
    }
}
