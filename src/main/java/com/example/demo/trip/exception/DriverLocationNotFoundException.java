package com.example.demo.trip.exception;

public class DriverLocationNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DriverLocationNotFoundException(String driverId) {
		super("Last known location not found for driver: " + driverId);
	}
}
