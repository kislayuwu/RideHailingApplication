package com.example.demo.Utils;

import org.springframework.stereotype.Component;

@Component
public class FareCalculator {

	private static final double BASE_FARE = 50;
	private static final double PER_KM_RATE = 12;
	private static final double DEFAULT_SURGE = 1.0;

	private final SurgePricingService surgePricingService;

	public FareCalculator(SurgePricingService surgePricingService) {
		this.surgePricingService = surgePricingService;
	}

	public double calculate(double distanceKm, String city) {

		if (distanceKm <= 0) {
			throw new InvalidFareRequestException("Distance must be greater than 0");
		}

		if (city == null || city.isBlank()) {
			throw new InvalidFareRequestException("City must be provided");
		}

		double baseFare = BASE_FARE + (distanceKm * PER_KM_RATE);

		double surgeMultiplier;
		try {
			surgeMultiplier = surgePricingService.getSurgeMultiplier(city);
		} catch (Exception ex) {
			// Fail-safe: do NOT block trip ending due to surge issues
			surgeMultiplier = DEFAULT_SURGE;
			throw new SurgePricingException("Failed to calculate surge pricing for city: " + city, ex);
		}

		return round(baseFare * surgeMultiplier);
	}

	private double round(double amount) {
		return Math.round(amount * 100.0) / 100.0;
	}
}
