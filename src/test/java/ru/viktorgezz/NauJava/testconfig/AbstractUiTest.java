package ru.viktorgezz.NauJava.testconfig;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.viktorgezz.NauJava.auth.dto.RegistrationRequest;
import ru.viktorgezz.NauJava.auth.service.AuthenticationService;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;

import java.time.Duration;

/**
 * Базовый класс для UI-тестов (Selenium).
 * Автоматически управляет жизненным циклом драйвера, настраивает ожидания
 * и создает тестового пользователя перед каждым тестом.
 */
public abstract class AbstractUiTest extends AbstractIntegrationPostgresTest {

    @LocalServerPort
    private int portServer;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private UserRepo userRepo;

    protected String urlLocalBase;

    protected WebDriver driverBrowser;
    protected WebDriverWait waitExplicit;

    protected RegistrationRequest userRegistered;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        setUrlLocalBase();
        setRegisterUser();
        setUpDriver();
    }

    @AfterEach
    void tearDown() {
        userRepo.deleteAll();
        if (driverBrowser != null) {
            driverBrowser.quit();
        }
    }

    private void setUrlLocalBase() {
        this.urlLocalBase = "http://localhost:" + portServer;
    }

    private void setRegisterUser() {
        this.userRegistered = new RegistrationRequest("user", "password", "password");
        authService.register(userRegistered, setRole());
    }

    private void setUpDriver() {
        ChromeOptions optionsChrome = new ChromeOptions();
        optionsChrome.addArguments("--remote-allow-origins=*");

        driverBrowser = new ChromeDriver(optionsChrome);
        waitExplicit = new WebDriverWait(driverBrowser, Duration.ofSeconds(10));

        driverBrowser.manage().window().maximize();
    }

    protected abstract Role setRole();
}
