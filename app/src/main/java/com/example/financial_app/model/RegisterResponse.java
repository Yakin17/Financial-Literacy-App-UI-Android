package com.example.financial_app.model;

public class RegisterResponse {
    private Long id;
    private String username;
    private String nom;
    private String email;
    private String role;

    public RegisterResponse() {}

    public RegisterResponse(Long id, String username, String nom, String email, String role) {
        this.id = id;
        this.username = username;
        this.nom = nom;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
