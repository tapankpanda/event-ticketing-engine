package com.tapan.eventticketingengine.auth.service;

import com.tapan.eventticketingengine.auth.dto.LoginRequest;
import com.tapan.eventticketingengine.auth.dto.LoginResponse;
import com.tapan.eventticketingengine.auth.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class LoginService {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginService(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        // Validate BCrypt maximum password length
        int passwordBytes = request.password()
                .getBytes(StandardCharsets.UTF_8)
                .length;

        if (passwordBytes > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        // Authenticate the submitted credentials
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        // Retrieve the authenticated user details
        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        // Generate a signed JWT
        String accessToken = jwtService.generateToken(userDetails);

        // Return the token and its configured lifetime
        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }
}
