package com.imatalk.chatservice.service;

import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String SECRET_KEY = "462D4A614E645267556B58703272357538782F413F4428472B4B625065536856";


    public String extractEmail(String jwt) {
        return extractClaim(jwt, claims -> claims.get("email", String.class));
    }

    public String extractId(String jwt) {
        return extractClaim(jwt, claims -> claims.get("id", String.class));
    }

    private <T> T extractClaim(String jwt, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwt);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String jwt) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception e) {
            throw new ApplicationException("Invalid or expired token");
        }

    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String validateToken(String jwt, User userDetails) {
        final String email = extractEmail(jwt);
        return (email.equals(userDetails.getEmail()) && !isTokenExpired(jwt)) ? email : null;
    }

    private boolean isTokenExpired(String jwt) {
        return extractExpiration(jwt).before(new Date());
    }

    private Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    public String generateToken(User user) {

        Map<String, String> claims = Map.of("id", user.getId(),
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "email", user.getEmail());

        return Jwts
                .builder()
                .setSubject(user.getEmail())
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365)) // 1 year
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}