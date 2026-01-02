package com.example.demo.payment.dto;

public class PaymentRequest {
	private String tripId;
	private Paymentmethod method;

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public Paymentmethod getMethod() {
		return method;
	}

	public void setMethod(Paymentmethod method) {
		this.method = method;
	}

}
