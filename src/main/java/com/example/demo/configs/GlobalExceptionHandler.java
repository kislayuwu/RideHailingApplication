package com.example.demo.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.driver.exception.DriverNotFoundException;
import com.example.demo.driver.exception.RideOfferExpiredException;
import com.example.demo.payment.exception.PaymentException;
import com.example.demo.ride.exception.RideNotFoundException;
import com.example.demo.trip.exception.DriverLocationNotFoundException;
import com.example.demo.trip.exception.InvalidTripStateException;
import com.example.demo.trip.exception.TripNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(DriverNotFoundException.class)
	public ResponseEntity<ApiError> handleDriverNotFound(DriverNotFoundException ex) {
		log.error("Driver error", ex);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("DRIVER_NOT_FOUND", ex.getMessage()));
	}

	@ExceptionHandler(RideOfferExpiredException.class)
	public ResponseEntity<ApiError> handleOfferExpired(RideOfferExpiredException ex) {
		log.warn("Ride offer issue", ex);
		return ResponseEntity.status(HttpStatus.GONE).body(new ApiError("OFFER_EXPIRED", ex.getMessage()));
	}

	@ExceptionHandler(RideNotFoundException.class)
	public ResponseEntity<ApiError> handleRideNotFound(RideNotFoundException ex) {
		log.error("Ride error", ex);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("RIDER_NOT_FOUND", ex.getMessage()));
	}

	@ExceptionHandler(TripNotFoundException.class)
	public ResponseEntity<ApiError> handleTripNotFound(TripNotFoundException ex) {
		log.error("Trip not found", ex);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("TRIP_NOT_FOUND", ex.getMessage()));
	}

	@ExceptionHandler(DriverLocationNotFoundException.class)
	public ResponseEntity<ApiError> handleDriverLocationNotFound(DriverLocationNotFoundException ex) {
		log.error("Driver location missing", ex);
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiError("DRIVER_LOCATION_NOT_FOUND", ex.getMessage()));
	}

	@ExceptionHandler(InvalidTripStateException.class)
	public ResponseEntity<ApiError> handleInvalidTripState(InvalidTripStateException ex) {
		log.warn("Invalid trip state", ex);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("INVALID_TRIP_STATE", ex.getMessage()));
	}

	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<ApiError> handlePaymentException(PaymentException ex) {
		log.error("Payment error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("PAYMENT_FAILED", ex.getMessage()));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("INVALID_STATE", ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception ex) {
		log.error("Unexpected error", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiError("INTERNAL_ERROR", "Something went wrong"));
	}
}
