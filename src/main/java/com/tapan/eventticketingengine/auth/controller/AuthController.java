package com.tapan.eventticketingengine.auth.controller;

import com.tapan.eventticketingengine.auth.dto.LoginRequest;
import com.tapan.eventticketingengine.auth.dto.LoginResponse;
import com.tapan.eventticketingengine.auth.dto.RegisterRequest;
import com.tapan.eventticketingengine.auth.dto.RegistrationResponse;
import com.tapan.eventticketingengine.auth.service.LoginService;
import com.tapan.eventticketingengine.auth.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;

    public AuthController(RegistrationService registrationService,
                          LoginService loginService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ){
        RegistrationResponse response = registrationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = loginService.login(request);

        return ResponseEntity.ok(response);
    }
}
