package ru.viktorgezz.NauJava.domain.user.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.viktorgezz.NauJava.domain.user.dto.UserResponseDto;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.repo.UserRepo;
import ru.viktorgezz.NauJava.domain.user.service.intrf.UserQueryService;
import ru.viktorgezz.NauJava.security.util.CurrentUserUtils;

import java.util.List;

import static ru.viktorgezz.NauJava.security.util.CurrentUserUtils.getCurrentUser;

/**
 * Реализация сервиса чтения пользователей. Реализует {@link UserQueryService}.
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
    public List<User> findAllByRole(Role role) {
        return userRepo.findAllByRole(role);
    }

    @Override
    public long computeCountUsers() {
        return userRepo.getCountUsers();
    }

    @Override
    public UserResponseDto getUserDtoFromSecurityContext() {
        User user = getCurrentUser();
        return new UserResponseDto(user.getUsername(), user.getRole());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Username " + username + " not found")
        );
    }
}
