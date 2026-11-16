package com.tapan.eventticketingengine.auth.service;

import com.tapan.eventticketingengine.auth.dto.RegisterRequest;
import com.tapan.eventticketingengine.auth.dto.RegistrationResponse;
import com.tapan.eventticketingengine.auth.exception.EmailAlreadyRegisteredException;
import com.tapan.eventticketingengine.user.entity.User;
import com.tapan.eventticketingengine.user.enums.UserRole;
import com.tapan.eventticketingengine.user.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class RegistrationService {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegistrationResponse register(RegisterRequest request){

        String email = request.email();

        if(userRepository.findByEmail(email).isPresent()){
            throw new EmailAlreadyRegisteredException(
                    "An account with this email address is already registered"
            );
        }

        // Validate BCrypt's maximum input length
        int passwordBytes = request.password()
                .getBytes(StandardCharsets.UTF_8)
                .length;

        if(passwordBytes > BCRYPT_MAX_PASSWORD_BYTES){
            throw new IllegalArgumentException(
                    "Password must not exceed 72 UTF-8 bytes"
            );
        }

        // Hash password before storing it
        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                request.fullName(),
                email,
                passwordHash,
                UserRole.CUSTOMER
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new RegistrationResponse(
                    savedUser.getId(),
                    savedUser.getFullName(),
                    savedUser.getEmail(),
                    savedUser.getRole(),
                    savedUser.getCreatedAt()
            );
        } catch (DataIntegrityViolationException exception){
            throw new EmailAlreadyRegisteredException(
                    "An account with this email address is already registered"
            );
        }
    }
}
