package ru.viktorgezz.NauJava.ui_tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.testconfig.AbstractUiTest;

import java.util.Objects;

@DisplayName("UI Tests: Authentication Scenario")
class AuthUiTest extends AbstractUiTest {

    @Test
    @DisplayName("Успешная авторизация, проверка перенаправления и выход из системы")
    void shouldLoginAndLogoutSuccessfully() {
        driverBrowser.get(urlLocalBase + "/ui/auth/login");

        WebElement inputName = waitExplicit.until(ExpectedConditions.visibilityOfElementLocated(By.name("name")));
        WebElement inputPassword = driverBrowser.findElement(By.name("password"));
        WebElement buttonLogin = driverBrowser.findElement(By.cssSelector("button[type='submit']"));

        inputName.sendKeys("user");
        inputPassword.sendKeys("password");
        buttonLogin.click();

        WebElement headerPage = waitExplicit.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        String textHeader = headerPage.getText();

        Assertions.assertEquals("Tests", textHeader);
        Assertions.assertTrue(Objects.requireNonNull(driverBrowser.getCurrentUrl()).contains("/ui/tests"));

        WebElement buttonLogout = waitExplicit.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[text()='Logout']")
        ));
        buttonLogout.click();

        waitExplicit.until(ExpectedConditions.titleIs("Login"));
        String titlePage = driverBrowser.getTitle();
        WebElement formLogin = driverBrowser.findElement(By.tagName("form"));

        Assertions.assertEquals("Login", titlePage);
        Assertions.assertTrue(formLogin.isDisplayed());
    }

    @Override
    protected Role setRole() {
        return Role.USER;
    }
}