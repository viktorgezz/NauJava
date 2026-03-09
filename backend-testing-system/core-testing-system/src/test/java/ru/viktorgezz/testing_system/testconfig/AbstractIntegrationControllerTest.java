package ru.viktorgezz.testing_system.testconfig;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.viktorgezz.security.service.JwtService;
import ru.viktorgezz.testing_system.auth.dto.RegistrationRequest;
import ru.viktorgezz.testing_system.auth.service.AuthenticationService;
import ru.viktorgezz.testing_system.domain.user.Role;
import ru.viktorgezz.testing_system.domain.user.repo.UserRepo;

import java.util.Arrays;
import java.util.Collection;

/**
 * Базовый класс для API-тестов через RestAssured.
 * Подготавливает окружение: настраивает порт, регистрирует пользователя
 * и инициализирует спецификацию запроса с готовым JWT-токеном.
 */
public abstract class AbstractIntegrationControllerTest extends AbstractIntegrationPostgresTest {

    @LocalServerPort
    private int portServer;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationService authService;

    protected RequestSpecification requestSpec;

    private static final String USERNAME_TEST = "test_reporter";

    @BeforeEach
    void setUp() {
        configureRestAssured();
        registerTestUser();
        initRequestSpecification();
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
    }

    private void configureRestAssured() {
        RestAssured.port = portServer;
    }

    private void registerTestUser() {
        authService.register(
                new RegistrationRequest(
                        USERNAME_TEST,
                        "password",
                        "password"),
                setRole()
        );
    }

    private void initRequestSpecification() {
        String token = jwtService.generateAccessToken(new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Arrays.stream(Role.values())
                        .map(role ->
                                new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .toList();
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getUsername() {
                return USERNAME_TEST;
            }
        });
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    protected abstract Role setRole();
}
