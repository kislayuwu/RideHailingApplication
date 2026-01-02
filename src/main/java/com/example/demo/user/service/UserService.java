package com.example.demo.user.service;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.user.dto.CreateUserRequest;
import com.example.demo.user.dto.CreateUserResponse;
import com.example.demo.user.dto.LoginRequest;
import com.example.demo.user.entities.User;
import com.example.demo.user.entities.UserDetailsResponse;
import com.example.demo.user.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
				.password(user.getPassword()).authorities(user.getRole().name()).build();
	}

	public UserDetailsResponse getUserDetails(LoginRequest request) {

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		UserDetailsResponse userDetails = new UserDetailsResponse();
		userDetails.setUserName(request.getEmail());
		userDetails.setRole(user.getRole().toString());

		return userDetails;
	}

	public CreateUserResponse register(CreateUserRequest request) {

		userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
			throw new IllegalArgumentException("Email already exists");
		});

		User user = new User();
		user.setId(UUID.randomUUID().toString());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(request.getRole());

		userRepository.save(user);

		return new CreateUserResponse(user.getId(), user.getEmail(), user.getRole());
	}
}
