package ru.viktorgezz.NauJava.domain.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.viktorgezz.NauJava.domain.user.Role;
import ru.viktorgezz.NauJava.domain.user.User;
import ru.viktorgezz.NauJava.domain.user.UserMapper;
import ru.viktorgezz.NauJava.domain.user.dto.UserResponseDto;
import ru.viktorgezz.NauJava.domain.user.dto.UserResponseOldDto;
import ru.viktorgezz.NauJava.domain.user.service.intrf.UserQueryService;

import java.util.List;
import java.util.stream.Collectors;

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
                .collect(Collectors.toList());
    }

    @GetMapping("/me")
    public UserResponseDto getUser() {
        return userQueryService.getUserDtoFromSecurityContext();
    }
}
