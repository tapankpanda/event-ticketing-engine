package com.tapan.eventticketingengine.auth.security;

import com.tapan.eventticketingengine.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        // Continue without JWT authentication if there is no Bearer token.
        if (authorizationHeader == null
                || !authorizationHeader.regionMatches(
                true, 0, "Bearer ", 0, 7
        )
                || SecurityContextHolder.getContext()
                .getAuthentication() != null) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract the email from the verified JWT.
            String email = jwtService.extractUsername(token);

            // Load the latest user details and authorities from MySQL.
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

            // Authenticate only if the JWT is valid for this user.
            if (jwtService.isTokenValid(token, userDetails)) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (
                JwtException
                | IllegalArgumentException
                | AuthenticationException exception
        ) {
            // Invalid, expired, or unknown-user tokens are not authenticated
            // The security entry point will handle protected requests
        }

        filterChain.doFilter(request, response);
    }
}
