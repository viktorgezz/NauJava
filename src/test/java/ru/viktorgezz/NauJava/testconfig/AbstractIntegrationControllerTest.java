package ru.viktorgezz.NauJava.testconfig;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.viktorgezz.NauJava.auth.dto.RegistrationRequest;
import ru.viktorgezz.NauJava.auth.service.AuthenticationService;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.security.service.JwtService;

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
        String token = jwtService.generateAccessToken(USERNAME_TEST);
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    abstract protected Role setRole();
}
