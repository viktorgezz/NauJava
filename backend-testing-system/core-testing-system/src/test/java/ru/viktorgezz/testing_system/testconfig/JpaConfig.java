package ru.viktorgezz.testing_system.testconfig;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"ru.viktorgezz.testing_system", "ru.viktorgezz.security"})
@EntityScan(basePackages = {"ru.viktorgezz.testing_system", "ru.viktorgezz.security"})
public class JpaConfig {
}
