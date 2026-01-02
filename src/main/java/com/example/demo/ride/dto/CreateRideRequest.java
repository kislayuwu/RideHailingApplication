package com.example.demo.ride.dto;

import jakarta.validation.constraints.NotNull;

public class CreateRideRequest {

	@NotNull
	private String city;

	@NotNull
	private Double pickupLat;

	@NotNull
	private Double pickupLng;

	@NotNull
	private Double destinationLat;

	@NotNull
	private Double destinationLng;

	public Double getPickupLat() {
		return pickupLat;
	}

	public void setPickupLat(Double pickupLat) {
		this.pickupLat = pickupLat;
	}

	public Double getPickupLng() {
		return pickupLng;
	}

	public void setPickupLng(Double pickupLng) {
		this.pickupLng = pickupLng;
	}

	public Double getDestinationLat() {
		return destinationLat;
	}

	public void setDestinationLat(Double destinationLat) {
		this.destinationLat = destinationLat;
	}

	public Double getDestinationLng() {
		return destinationLng;
	}

	public void setDestinationLng(Double destinationLng) {
		this.destinationLng = destinationLng;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

}
