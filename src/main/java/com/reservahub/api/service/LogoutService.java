package com.reservahub.api.service;

import com.reservahub.api.security.JwtService;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {

    private final JwtService jwtService;

    public LogoutService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void logout(String token) {
        jwtService.revogarToken(token);
    }
}