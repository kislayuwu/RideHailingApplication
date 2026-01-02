package com.example.demo.user.dto;

import com.example.demo.user.entities.Role;

public class CreateUserResponse {

	private String id;
	private String email;
	private Role role;

	public CreateUserResponse(String id, String email, Role role) {
		this.id = id;
		this.email = email;
		this.role = role;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}
