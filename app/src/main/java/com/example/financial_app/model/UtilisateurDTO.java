package com.example.financial_app.model;

import java.time.LocalDateTime;

public class UtilisateurDTO {
    private Long id;
    private String nom;
    private String username;
    private String email;
    private String role;
    private LocalDateTime dateCreation;

    // Constructeur par défaut
    public UtilisateurDTO() {}

    // Constructeur avec paramètres
    public UtilisateurDTO(Long id, String nom, String username, String email, String role, LocalDateTime dateCreation) {
        this.id = id;
        this.nom = nom;
        this.username = username;
        this.email = email;
        this.role = role;
        this.dateCreation = dateCreation;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "UtilisateurDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}