package com.example.demo.ride.exception;

public class RideNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RideNotFoundException(String id) {
		super("Ride not found: " + id);
	}
}
