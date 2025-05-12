package com.example.financial_app.model;

public class RegisterRequest {
    private String username;
    private String nom;
    private String email;
    private String motDePasse;
    private String role;

    public RegisterRequest(String username, String nom, String email, String motDePasse) {
        this.username = username;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = "ROLE_USER";
    }

    public RegisterRequest(String username, String nom, String email, String motDePasse, String role) {
        this.username = username;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
