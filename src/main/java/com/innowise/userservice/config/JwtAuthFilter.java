package com.innowise.userservice.config;

import com.innowise.userservice.entity.UserRole;
import com.innowise.userservice.exception.TokenException;
import com.innowise.userservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                jwtService.isTokenValid(token);
                String tokenType = jwtService.extractTokenType(token);
                if ("access".equals(tokenType)) {
                    String roleFromToken = jwtService.extractRole(token);
                    UserRole role = UserRole.valueOf(roleFromToken);

                    Object principal = "SERVICE".equals(roleFromToken)
                            ? "SERVICE"
                            : jwtService.extractUserId(token);

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(role)
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (TokenException e) {
                log.warn("Token validation failed: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}