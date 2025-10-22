package ru.viktorgezz.NauJava.user;

import jakarta.persistence.*;
import ru.viktorgezz.NauJava.test.TestModel;
import ru.viktorgezz.NauJava.result.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * Модель пользователя системы тестирования.
 */
@Entity
@Table(name = "users")
public class User {

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

    public User() {
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
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

    public String getUsername() {
        return username;
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

    public void setTests(List<TestModel> tests) {
        this.tests = tests;
    }

    public List<Result> getTestResults() {
        return results;
    }

    public void setTestResults(List<Result> results) {
        this.results = results;
    }
}
