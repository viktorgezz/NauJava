package ru.viktorgezz.NauJava.user.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.User;
import ru.viktorgezz.NauJava.user.repo.UserRepo;
import ru.viktorgezz.NauJava.user.service.intrf.UserQueryService;

import java.util.List;

/**
 * Реализация сервиса чтения пользователей. Реализует {@link ru.viktorgezz.NauJava.user.service.intrf.UserQueryService}.
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepo userRepo;

    public UserQueryServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User getByUsername(String username) {
        return userRepo.findByUsername(username).orElseThrow(() ->
                new BusinessException(ErrorCode.USER_NOT_FOUND, username));
    }

    @Override
    public User getById(Long id) {
        return userRepo.findById(id).orElseThrow(() ->
                new EntityNotFoundException("User with ID " + id + " not found"));
    }

    @Override
    public List<User> findAllByRole(Role role) {
        return userRepo.findAllByRole(role);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Username " + username + " not found")
        );
    }
}
