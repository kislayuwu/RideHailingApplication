package com.example.demo.driver.exception;

public class RideOfferExpiredException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RideOfferExpiredException(String message) {
		super(message);
	}
}
