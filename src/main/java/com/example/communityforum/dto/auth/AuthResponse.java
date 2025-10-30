package com.example.communityforum.dto.auth;

import com.example.communityforum.dto.user.UserResponseDTO;

public class AuthResponse {
    private String token;
    private UserResponseDTO user;

    public AuthResponse(String token,UserResponseDTO user) {
        this.token = token;
        this.user = user;
    }

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public UserResponseDTO getUser() {
        return user;
    }
}
