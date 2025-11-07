package ru.viktorgezz.NauJava.auth.service;

import ru.viktorgezz.NauJava.auth.dto.AuthenticationRequest;
import ru.viktorgezz.NauJava.auth.dto.AuthenticationResponse;
import ru.viktorgezz.NauJava.auth.dto.RefreshRequest;
import ru.viktorgezz.NauJava.auth.dto.RegistrationRequest;
import ru.viktorgezz.NauJava.domain.user.Role;

public interface AuthenticationService {

    AuthenticationResponse login(AuthenticationRequest request);

    void register(RegistrationRequest request, Role role);

    AuthenticationResponse refreshToken(RefreshRequest request);

    void logout(String refreshToken);
}
