package com.innowise.userservice.dto;

import com.innowise.userservice.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UserDto {

    private Long id;

    public static final int MIN_NAME_LENGHT = 2;
    public static final int MAX_NAME_LENGHT = 100;

    @NotBlank(message = "Name is required")
    @Size(min = MIN_NAME_LENGHT, max = MAX_NAME_LENGHT, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Surname is required")
    @Size(min = MIN_NAME_LENGHT, max = MAX_NAME_LENGHT, message = "Surname must be between 2 and 100 characters")
    private String surname;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private UserRole role;

    private boolean active;

    public UserDto() {}

    public UserDto(Long id, String name, String surname, LocalDate birthDate, String email, boolean active) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.email = email;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}