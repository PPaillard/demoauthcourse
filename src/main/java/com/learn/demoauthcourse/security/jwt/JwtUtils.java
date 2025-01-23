package com.learn.demoauthcourse.security.jwt;


import com.learn.demoauthcourse.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${demoauthcourse.app.jwtSecret}")
    private String jwtSecret;

    @Value("${demoauthcourse.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${demoauthcourse.app.jwtCookieName}")
    private String jwtCookie;


    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        // on génère le token
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        // on créé un cookie nommé, qui stockera le token et aura une date d'expiration
        // Par sécurité, il ne sera pas accessible via JS
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt).path("/api").maxAge(24 * 60 * 60).httpOnly(true).build();
        return cookie;
    }
    // Clé pour encoder la signature du token
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    // Génère le token grace au username, une date de création, et une date d'expiration.
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }
}