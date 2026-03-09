package ru.viktorgezz.testing_system.domain.user.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.viktorgezz.testing_system.domain.user.dto.UserResponseDto;
import ru.viktorgezz.testing_system.exception.BusinessException;
import ru.viktorgezz.testing_system.exception.ErrorCode;
import ru.viktorgezz.testing_system.domain.user.Role;
import ru.viktorgezz.testing_system.domain.user.User;
import ru.viktorgezz.testing_system.domain.user.repo.UserRepo;
import ru.viktorgezz.testing_system.domain.user.service.intrf.UserQueryService;

import java.util.List;

import static ru.viktorgezz.testing_system.domain.util.CurrentUserUtils.getCurrentUser;

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
