package com.example.demo.Utils;

import java.security.Key;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private static final String SECRET = "super-secret-key-super-secret-key-123456";
	private static final long ACCESS_EXP = 1000 * 60 * 15;
	private static final long REFRESH_EXP = 1000 * 60 * 60 * 24;

	private Key key() {
		return Keys.hmacShaKeyFor(SECRET.getBytes());
	}

	public String generateAccessToken(UserDetails user) {
		return Jwts.builder().setSubject(user.getUsername())
				.claim("role", user.getAuthorities().iterator().next().getAuthority()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP))
				.signWith(key(), SignatureAlgorithm.HS256).compact();
	}

	public String generateRefreshToken(UserDetails user) {
		return Jwts.builder().setSubject(user.getUsername()).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP))
				.signWith(key(), SignatureAlgorithm.HS256).compact();
	}

	public Claims validate(String token) {
		return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
	}

	public String extractUsername(String token) {
		return validate(token).getSubject();
	}
}
