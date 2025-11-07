package ru.viktorgezz.NauJava.domain.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.viktorgezz.NauJava.security.RefreshToken;
import ru.viktorgezz.NauJava.domain.test.TestModel;
import ru.viktorgezz.NauJava.domain.result.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Модель пользователя системы тестирования.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_enabled")
    private boolean enabled;

    @Column(name = "is_account_locked")
    private boolean locked;

    @Column(name = "is_credentials_expired")
    private boolean credentialsExpired;

    @OneToMany(
            mappedBy = "author",
            fetch = FetchType.LAZY
    )
    private List<TestModel> tests = new ArrayList<>();

    @OneToMany(
            mappedBy = "participant",
            fetch = FetchType.LAZY
    )
    private List<Result> results = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<RefreshToken> refreshTokens;

    public User() {
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User(String username, String password, Role role, boolean enabled, boolean locked, boolean credentialsExpired) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.locked = locked;
        this.credentialsExpired = credentialsExpired;
    }

    public User(String username, String password, Role role, List<TestModel> tests, List<Result> results) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.tests = tests;
        this.results = results;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<TestModel> getTests() {
        return tests;
    }

    public List<Result> getResults() {
        return results;
    }

    public List<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }
}
