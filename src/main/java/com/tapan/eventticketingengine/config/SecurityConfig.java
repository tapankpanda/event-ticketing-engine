package com.tapan.eventticketingengine.config;

import com.tapan.eventticketingengine.auth.security.JwtAuthenticationFilter;
import com.tapan.eventticketingengine.auth.security.JwtService;
import com.tapan.eventticketingengine.auth.security.RestAccessDeniedHandler;
import com.tapan.eventticketingengine.auth.security.RestAuthenticationEntryPoint;
import com.tapan.eventticketingengine.auth.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                // This application uses JWT, not browser-based login
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // Do not create or use HTTP sessions.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Register the database-backed authentication provider
                .authenticationProvider(authenticationProvider)

                // Return structured JSON for 401 and 403 responses.
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Public and protected endpoint rules
                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/register",
                                "/api/v1/auth/login"
                        ).permitAll()

                        // Public event and ticket-category browsing
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/events",
                                "/api/v1/events/**",
                                "/api/v1/ticket-types/**"
                        ).permitAll()

                        // Only ORGANIZER can access organizer endpoints
                        .requestMatchers(
                                "/api/v1/organizer/**"
                        ).hasRole("ORGANIZER")

                        // Only CUSTOMER can access booking endpoints
                        .requestMatchers(
                                "/api/v1/bookings/**"
                        ).hasRole("CUSTOMER")

                        // All remaining endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Process JWTs before Spring Security's username/password filter
                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtService,
                                userDetailsService
                        ),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
