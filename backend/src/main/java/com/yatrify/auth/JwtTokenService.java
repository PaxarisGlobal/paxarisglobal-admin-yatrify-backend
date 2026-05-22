package com.yatrify.auth;

import com.yatrify.config.properties.YatrifyProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final YatrifyProperties properties;

    public String createToken(String genericUserId, String email, List<String> roles) {
        long now = System.currentTimeMillis();
        long exp = now + properties.getJwt().getExpiration();

        return Jwts.builder()
                .subject(genericUserId)
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(exp))
                .signWith(Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
