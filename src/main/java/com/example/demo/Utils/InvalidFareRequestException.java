package com.example.demo.Utils;

public class InvalidFareRequestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidFareRequestException(String message) {
		super(message);
	}
}
