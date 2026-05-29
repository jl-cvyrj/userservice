package com.innowise.userservice.service;

import io.jsonwebtoken.Claims;

public interface JwtService {

    Claims extractAllClaims(String token);

    Long extractUserId(String token);

    String extractRole(String token);

    boolean isTokenValid(String token);
}