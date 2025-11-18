package ru.viktorgezz.NauJava.security;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import ru.viktorgezz.NauJava.testconfig.AbstractIntegrationPostgresTest;
import ru.viktorgezz.NauJava.auth.dto.RegistrationRequest;
import ru.viktorgezz.NauJava.auth.service.AuthenticationService;
import ru.viktorgezz.NauJava.security.service.JwtService;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;

import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SecurityAccessTest Integration Tests")
class SecurityAccessTest extends AbstractIntegrationPostgresTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestRestTemplateConfig {
        @Bean
        public TestRestTemplate testRestTemplate(RestTemplateBuilder builder) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, @NotNull String httpMethod) {
                    connection.setInstanceFollowRedirects(false);
                }
            };

            RestTemplateBuilder configuredBuilder = builder.requestFactory(() -> factory);
            return new TestRestTemplate(configuredBuilder);
        }
    }

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationService authenticationService;

    private static final String USERNAME_USER = "testuser";
    private static final String USERNAME_ADMIN = "testadmin";

    @BeforeEach
    void setUp() {
        authenticationService.register(new RegistrationRequest(USERNAME_USER, "pass", "pass"), Role.USER);
        authenticationService.register(new RegistrationRequest(USERNAME_ADMIN, "pass", "pass"), Role.ADMIN);
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
    }

    @Nested
    @DisplayName("Анонимный пользователь")
    class AnonymousAccess {

        @Test
        @DisplayName("Перенаправление на логин для защищённой конечной точки")
        void anonymous_isRedirected_onProtectedEndpoint() {
            TestRestTemplate noRedirect = restTemplate.withBasicAuth("", "");

            ResponseEntity<String> response = noRedirect.exchange(
                    getBaseUrl() + "/users/search/username?username=any",
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    String.class
            );

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
            assertTrue(response.getHeaders().getLocation() != null &&
                    response.getHeaders().getLocation().getPath().equals("/ui/auth/login"));
        }
        @Test
        @DisplayName("Перенаправление на логин для админского эндпоинта (swagger)")
        void anonymous_isRedirected_onAdminOnly() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    getBaseUrl() + "/swagger-ui.html",
                    String.class
            );

            assertTrue(response.getStatusCode() == HttpStatus.FOUND ||
                    response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                    response.getStatusCode() == HttpStatus.FORBIDDEN);
        }

    }
    @Nested
    @DisplayName("Доступ роли USER")
    class UserRoleAccess {

        @Test
        @DisplayName("USER имеет доступ к обычным защищённым эндпоинтам")
        void user_canAccess_protectedEndpoints() {
            HttpHeaders headers = createAuthHeaders(USERNAME_USER);
            ResponseEntity<String> response = restTemplate.exchange(
                    getBaseUrl() + "/users/search/username?username=testuser",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
        @Test
        @DisplayName("USER запрещён доступ к админскому эндпоинту (swagger)")
        void user_forbidden_onAdminOnly() {
            HttpHeaders headers = createAuthHeaders(USERNAME_USER);
            ResponseEntity<String> response = restTemplate.exchange(
                    getBaseUrl() + "/swagger-ui.html",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            assertEquals(HttpStatus.FOUND, response.getStatusCode());
        }

    }
    @Nested
    @DisplayName("Доступ роли ADMIN")
    class AdminRoleAccess {

        @Test
        @DisplayName("ADMIN имеет доступ к обычным защищённым эндпоинтам")
        void admin_canAccess_protectedEndpoints() {
            HttpHeaders headers = createAuthHeaders(USERNAME_ADMIN);
            ResponseEntity<String> response = restTemplate.exchange(
                    getBaseUrl() + "/users/search/username?username=testuser",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
        @Test
        @DisplayName("ADMIN имеет доступ к swagger")
        void admin_canAccess_swagger() {
            HttpHeaders headers = createAuthHeaders(USERNAME_ADMIN);
            ResponseEntity<String> response = restTemplate.exchange(
                    getBaseUrl() + "/swagger-ui/favicon-32x32.png",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private HttpHeaders createAuthHeaders(String username) {
        HttpHeaders headers = new HttpHeaders();
        String token = jwtService.generateAccessToken(username);
        headers.setBearerAuth(token);
        return headers;
    }
}