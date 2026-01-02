package com.example.demo.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.Utils.JwtUtil;
import com.example.demo.user.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtUtil jwtUtil;
	private final UserService userService;

	public SecurityConfig(JwtUtil jwtUtil, UserService userService) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
			throws Exception {

		JwtLoginFilter loginFilter = new JwtLoginFilter(authenticationManager, jwtUtil);

		JwtRefreshFilter refreshFilter = new JwtRefreshFilter(jwtUtil, userService);

		JwtAuthenticationFilter authFilter = new JwtAuthenticationFilter(jwtUtil, userService);

		http.cors(cors -> cors.configurationSource(request -> {
			var config = new org.springframework.web.cors.CorsConfiguration();
			config.setAllowCredentials(true);
			config.addAllowedOriginPattern("*"); // allow all origins
			config.addAllowedHeader("*");
			config.addAllowedMethod("*");
			return config;
		})).csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/user/register", "/ws/**").permitAll().anyRequest()
						.authenticated())
				.addFilter(loginFilter).addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(refreshFilter, JwtAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

}
