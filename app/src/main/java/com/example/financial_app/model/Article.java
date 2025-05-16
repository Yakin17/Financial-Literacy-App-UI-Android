package com.example.financial_app.model;
import java.io.Serializable;
import java.time.LocalDateTime;
public class Article implements Serializable {
    private Long id;
    private String titre;
    private String contenu;
    private LocalDateTime datePublication;
    private String auteurNom;
    private Long auteurId;
    private boolean hasQuiz;

    public Article() {

    }
    public Article(Long id, String titre, String contenu, LocalDateTime datePublication, String auteurNom, Long auteurId, boolean hasQuiz) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.auteurNom = auteurNom;
        this.auteurId = auteurId;
        this.hasQuiz = hasQuiz;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public void setAuteurNom(String auteurNom) {
        this.auteurNom = auteurNom;
    }

    public Long getAuteurId() {
        return auteurId;
    }

    public void setAuteurId(Long auteurId) {
        this.auteurId = auteurId;
    }

    public boolean isHasQuiz() {
        return hasQuiz;
    }

    public void setHasQuiz(boolean hasQuiz) {
        this.hasQuiz = hasQuiz;
    }
}
