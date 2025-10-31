package ru.viktorgezz.NauJava.security;

import jakarta.persistence.*;
import ru.viktorgezz.NauJava.user.User;

import java.util.Date;

/**
 * Сущность для хранения refresh-токенов пользователей.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(nullable = false, length = 1024, unique = true)
    private String refreshToken;

    @Column(nullable = false)
    private Date dateExpiration;

    public RefreshToken() {
    }

    public RefreshToken(User user, String refreshToken, Date dateExpiration) {
        this.user = user;
        this.refreshToken = refreshToken;
        this.dateExpiration = dateExpiration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
}
