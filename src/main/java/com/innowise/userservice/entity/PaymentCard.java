package com.innowise.userservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payment_cards", indexes = {
        @Index(name = "index_payment_card_number",
                columnList = "number"),
        @Index(name = "index_payment_card_user_id",
                columnList = "user_id, holder")
})
@EntityListeners(AuditingEntityListener.class)
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "number", nullable = false, unique = true, length = 16)
    private String number;

    @Column(name = "holder", nullable = false, length = 100)
    private String holder;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "active")
    private boolean active;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PaymentCard() {}

    public PaymentCard(User user, String number, String holder, LocalDate expirationDate, boolean active) {
        this.user = user;
        this.number = number;
        this.holder = holder;
        this.expirationDate = expirationDate;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getNumber() {
        return number;
    }

    public String getHolder() {
        return holder;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PaymentCard{");
        sb.append("id=").append(id);
        sb.append(", userId=").append(user != null ? user.getId() : null);
        sb.append(", number='").append(number).append('\'');
        sb.append(", holder='").append(holder).append('\'');
        sb.append(", expirationDate=").append(expirationDate);
        sb.append(", active=").append(active);
        sb.append('}');
        return sb.toString();
    }
}
