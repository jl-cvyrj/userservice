package com.innowise.userservice.service;

import com.innowise.userservice.exception.TokenException;
import io.jsonwebtoken.Claims;

public interface JwtService {

    Claims extractAllClaims(String token);

    Long extractUserId(String token);

    String extractRole(String token);

    void isTokenValid(String token) throws TokenException;

    public String extractTokenType(String token);
}