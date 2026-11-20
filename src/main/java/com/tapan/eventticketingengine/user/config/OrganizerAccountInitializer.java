package com.tapan.eventticketingengine.user.config;


import com.tapan.eventticketingengine.user.entity.User;
import com.tapan.eventticketingengine.user.enums.UserRole;
import com.tapan.eventticketingengine.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

@Component
@Profile("dev")
public class OrganizerAccountInitializer implements ApplicationRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(OrganizerAccountInitializer.class);

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String fullName;
    private final String email;
    private final String password;

    public OrganizerAccountInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.organizer.full-name}") String fullName,
            @Value("${app.organizer.email}") String email,
            @Value("${app.organizer.password}") String password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {

        String organizerName = fullName == null ? "" : fullName.strip();

        String organizerEmail = email == null
                ? ""
                : email.strip().toLowerCase(Locale.ROOT);

        // Validate the development organizer configuration.
        if (organizerName.isBlank()
                || organizerEmail.isBlank()
                || password == null
                || password.isBlank()) {

            throw new IllegalStateException(
                    "Development organizer configuration is incomplete"
            );
        }

        // Enforce BCrypt's maximum input length.
        int passwordBytes = password
                .getBytes(StandardCharsets.UTF_8)
                .length;

        if (passwordBytes > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new IllegalStateException(
                    "Development organizer password must not exceed "
                            + "72 UTF-8 bytes"
            );
        }

        // Check whether an account with this email already exists.
        Optional<User> existingUser =
                userRepository.findByEmail(organizerEmail);

        if (existingUser.isPresent()) {

            // Never silently treat a CUSTOMER account as an ORGANIZER.
            if (existingUser.get().getRole() != UserRole.ORGANIZER) {
                throw new IllegalStateException(
                        "The configured organizer email is already "
                                + "registered with a different role. "
                                + "Choose another organizer email."
                );
            }

            logger.info(
                    "Development organizer account already exists. "
                            + "Skipping initialization."
            );

            return;
        }

        // Create the organizer with a BCrypt-encoded password.
        User organizer = new User(
                organizerName,
                organizerEmail,
                passwordEncoder.encode(password),
                UserRole.ORGANIZER
        );

        userRepository.saveAndFlush(organizer);

        logger.info(
                "Development organizer account initialized successfully."
        );
    }
}
