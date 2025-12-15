package ru.viktorgezz.NauJava.domain.user.util;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.domain.user.service.intrf.UserCommandService;

import java.util.List;
import java.util.Optional;

/**
 * Компонент для автоматического создания главного администратора при старте приложения.
 * Активируется только при наличии свойства app.admin.main.create=true в конфигурации.
 */
@Component
@ConditionalOnProperty(name = "admin.main.create", havingValue = "true", matchIfMissing = false)
public class CreationMainAdmin {

    private static final Logger log = LoggerFactory.getLogger(CreationMainAdmin.class);

    private static final String DEFAULT_ADMIN_USERNAME = "admin_main";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin_main_password";

    private final UserRepo userRepo;
    private final UserCommandService userCommandService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CreationMainAdmin(
            UserRepo userRepo,
            UserCommandService userCommandService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepo = userRepo;
        this.userCommandService = userCommandService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void createMainAdminIfNotExists() {
        Optional<User> existingAdmin = userRepo.findByUsername(DEFAULT_ADMIN_USERNAME);

        List<User> adminsWithRole = userRepo.findAllByRole(Role.ADMIN_MAIN);
        if (!adminsWithRole.isEmpty()) {
            log.debug("Пользователь с ролью ADMIN_MAIN уже существует в базе данных. Пропускаем создание.");
            return;
        }

        User mainAdmin = new User(
                DEFAULT_ADMIN_USERNAME,
                passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                Role.ADMIN_MAIN,
                true,  // enabled
                false, // locked
                false  // credentialsExpired
        );

        userCommandService.save(mainAdmin);
        log.info("Username: {}, Password: {}, Role: {}", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD, Role.ADMIN_MAIN);
    }
}
