package com.tapan.eventticketingengine.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final Clock clock;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            Clock clock
    ) {

        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret must be configured"
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalArgumentException(
                    "JWT expiration must be greater than zero"
            );
        }

        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.clock = clock;
    }

    // Generate a signed JWT for an authenticated user
    public String generateToken(UserDetails userDetails) {

        Instant issuedAt = clock.instant();
        Instant expiration = issuedAt.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // Extract the email address from the JWT subject
    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Validate the token against the authenticated user's details
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            Date expiration = claims.getExpiration();

            return username != null
                    && username.equals(userDetails.getUsername())
                    && expiration != null
                    && expiration.after(Date.from(clock.instant()));

        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    // Verify the signature and parse the signed JWT claims
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Return the configured token lifetime in seconds
    public long getExpirationSeconds() {

        return expirationMs / 1000;
    }
}
