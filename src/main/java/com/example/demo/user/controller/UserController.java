package com.example.demo.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.user.dto.CreateUserRequest;
import com.example.demo.user.dto.CreateUserResponse;
import com.example.demo.user.dto.LoginRequest;
import com.example.demo.user.entities.UserDetailsResponse;
import com.example.demo.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<CreateUserResponse> register(@Valid @RequestBody CreateUserRequest request) {

		return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
	}

	@PostMapping("/details")
	public ResponseEntity<UserDetailsResponse> login(@Valid @RequestBody LoginRequest request) {

		return ResponseEntity.status(HttpStatus.OK).body(userService.getUserDetails(request));
	}

}
