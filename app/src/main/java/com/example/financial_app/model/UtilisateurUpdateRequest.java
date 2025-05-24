package com.example.financial_app.model;

public class UtilisateurUpdateRequest {
    private String nom;
    private String username;
    private String email;
    private String motDePasse;
    private String role;

    // Constructeur par défaut
    public UtilisateurUpdateRequest() {}

    // Constructeur avec paramètres
    public UtilisateurUpdateRequest(String nom, String username, String email, String motDePasse, String role) {
        this.nom = nom;
        this.username = username;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UtilisateurUpdateRequest{" +
                "nom='" + nom + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", motDePasse='" + (motDePasse != null ? "[PROTECTED]" : "null") + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}