package com.example.demo.trip.exception;

public class InvalidTripStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidTripStateException(String message) {
		super(message);
	}
}