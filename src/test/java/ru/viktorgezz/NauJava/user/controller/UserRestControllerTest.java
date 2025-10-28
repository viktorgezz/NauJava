package ru.viktorgezz.NauJava.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.viktorgezz.NauJava.user.Role;
import ru.viktorgezz.NauJava.user.service.intrf.UserQueryService;
import ru.viktorgezz.NauJava.util.GeneratorRandomModel;
import ru.viktorgezz.NauJava.exception.BusinessException;
import ru.viktorgezz.NauJava.exception.ErrorCode;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты веб-слоя для {@link UserRestController}.
 */
@WebMvcTest(UserRestController.class)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQueryService userQueryService;

    @Test
    @DisplayName("GET /users/search/username возвращает пользователя по username")
    void getUserByUsername_returnsUser() throws Exception {
        var user = GeneratorRandomModel.getRandomUser();
        user.setUsername("alice");
        user.setId(100L);

        given(userQueryService.getByUsername("alice"))
                .willReturn(user);

        mockMvc.perform(get("/users/search/username")
                        .param("username", "alice")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.id").value(100));

        verify(userQueryService).getByUsername("alice");
        verifyNoMoreInteractions(userQueryService);
    }

    @Test
    @DisplayName("GET /users/search/username возвращает 404 и тело ошибки при отсутствии пользователя")
    void getUserByUsername_userNotFound_returns404() throws Exception {
        given(userQueryService.getByUsername("ghost"))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND, "ghost"));

        mockMvc.perform(get("/users/search/username")
                        .param("username", "ghost")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("User not found"))
                .andExpect(jsonPath("$.message").value("User with username: ghost - not found"));

        verify(userQueryService).getByUsername("ghost");
        verifyNoMoreInteractions(userQueryService);
    }

    @Test
    @DisplayName("GET /users/search/role возвращает пользователей по роли")
    void getUsersByRole_returnsUsers() throws Exception {
        var user1 = GeneratorRandomModel.getRandomUser();
        user1.setId(1L);
        user1.setRole(Role.ADMIN);
        user1.setUsername("admin1");

        var user2 = GeneratorRandomModel.getRandomUser();
        user2.setId(2L);
        user2.setRole(Role.ADMIN);
        user2.setUsername("admin2");

        given(userQueryService.findAllByRole(Role.ADMIN))
                .willReturn(List.of(user1, user2));

        mockMvc.perform(get("/users/search/role")
                        .param("role", "ADMIN")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='admin1')]").exists())
                .andExpect(jsonPath("$[?(@.username=='admin2')]").exists());

        verify(userQueryService).findAllByRole(Role.ADMIN);
        verifyNoMoreInteractions(userQueryService);
    }
}