package ru.viktorgezz.testing_system.domain.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.viktorgezz.testing_system.domain.user.Role;
import ru.viktorgezz.testing_system.domain.user.User;
import ru.viktorgezz.testing_system.domain.user.UserMapper;
import ru.viktorgezz.testing_system.domain.user.dto.UserResponseDto;
import ru.viktorgezz.testing_system.domain.user.dto.UserResponseOldDto;
import ru.viktorgezz.testing_system.domain.user.service.intrf.UserQueryService;

import java.util.List;

/**
 * REST-контроллер для пользователей {@link User}.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserQueryService userQueryService;

    @Autowired
    public UserController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/search/username")
    public UserResponseOldDto getUserByUsername(@RequestParam String username) {
        return UserMapper.toDto(userQueryService.getByUsername(username));
    }

    @GetMapping("/search/role")
    public List<UserResponseOldDto> getUsersByRole(@RequestParam Role role) {
        return userQueryService.findAllByRole(role)
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @GetMapping("/me")
    public UserResponseDto getUser() {
        return userQueryService.getUserDtoFromSecurityContext();
    }
}
