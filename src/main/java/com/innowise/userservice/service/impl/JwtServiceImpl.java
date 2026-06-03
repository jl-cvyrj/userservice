package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.TokenException;
import com.innowise.userservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public void isTokenValid(String token) throws TokenException {
        try {
            extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            throw new TokenException("Token expired");
        } catch (MalformedJwtException | SignatureException e) {
            throw new TokenException("Invalid token");
        } catch (Exception e) {
            throw new TokenException("Token validation failed");
        }
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }
}