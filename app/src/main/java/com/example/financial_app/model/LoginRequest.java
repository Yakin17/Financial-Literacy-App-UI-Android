package com.example.financial_app.model;

import java.util.regex.Pattern;

public class LoginRequest {
    private String email;
    private String motDePasse;

    public LoginRequest(String email, String motDePasse) {
        setEmail(email);
        setMotDePasse(motDePasse);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("Email invalide");
        }
        this.email = email.trim();
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit faire au moins 6 caractères");
        }
        this.motDePasse = motDePasse;
    }

    // Email validation method
    private boolean isValidEmail(String email) {
        if (email == null) return false;

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);

        return pattern.matcher(email.trim()).matches();
    }
}