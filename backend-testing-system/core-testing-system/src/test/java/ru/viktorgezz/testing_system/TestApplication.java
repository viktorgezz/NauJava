package ru.viktorgezz.testing_system;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"ru.viktorgezz.testing_system", "ru.viktorgezz.security"})
@EnableScheduling
@EnableAsync
public class TestApplication {
}
