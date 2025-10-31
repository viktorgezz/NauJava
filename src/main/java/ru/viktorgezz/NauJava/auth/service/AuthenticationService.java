package ru.viktorgezz.NauJava.auth.service;

import ru.viktorgezz.NauJava.auth.dto.AuthenticationRequest;
import ru.viktorgezz.NauJava.auth.dto.AuthenticationResponse;
import ru.viktorgezz.NauJava.auth.dto.RefreshRequest;
import ru.viktorgezz.NauJava.auth.dto.RegistrationRequest;

public interface AuthenticationService {

    AuthenticationResponse login(AuthenticationRequest request);

    void register(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);

    void logout(String refreshToken);
}
