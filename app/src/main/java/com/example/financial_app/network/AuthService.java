package com.example.financial_app.network;

import com.example.financial_app.model.LoginRequest;
import com.example.financial_app.model.LoginResponse;
import com.example.financial_app.model.RegisterRequest;
import com.example.financial_app.model.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);
}