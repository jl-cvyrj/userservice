package com.innowise.userservice.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class PaymentCardDto {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Card number is required")
    @Size(min = 16, max = 19, message = "Card number must be between 16 and 19 characters")
    @Pattern(regexp = "\\d+", message = "Card number must contain only digits")
    private String number;

    @NotBlank(message = "Holder name is required")
    @Size(min = 2, max = 100, message = "Holder name must be between 2 and 100 characters")
    private String holder;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    private boolean active;

    public PaymentCardDto() {}

    public PaymentCardDto(Long id, Long userId, String number, String holder, LocalDate expirationDate, boolean active) {
        this.id = id;
        this.userId = userId;
        this.number = number;
        this.holder = holder;
        this.expirationDate = expirationDate;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}