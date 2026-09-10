package com.innowise.userservice.entity;

import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ADMIN,
    USER,
    SERVICE;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}