package com.example.demo.trip.exception;

public class TripNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TripNotFoundException(String tripId) {
		super("Trip not found: " + tripId);
	}
}
