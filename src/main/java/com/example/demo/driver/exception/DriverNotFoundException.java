package com.example.demo.driver.exception;

public class DriverNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DriverNotFoundException(String id) {
        super("Driver not found: " + id);
    }
}
