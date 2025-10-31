package ru.viktorgezz.NauJava.user.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;
import ru.viktorgezz.NauJava.user.service.intrf.UserCommandService;

/**
 * Сервис команд над пользователями. Реализует {@link ru.viktorgezz.NauJava.user.service.intrf.UserCommandService}.
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepo userRepo;

    @Autowired
    public UserCommandServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public void save(User user) {
        userRepo.save(user);
    }

}
