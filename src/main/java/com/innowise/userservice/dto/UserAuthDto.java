package com.innowise.userservice.dto;

public class UserAuthDto {
    private Long id;
    private String email;
    private String role;
    private boolean active;

    public UserAuthDto() {}

    public UserAuthDto(Long id, String email, String role, boolean active) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id; }
    public String getEmail() { return email;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}