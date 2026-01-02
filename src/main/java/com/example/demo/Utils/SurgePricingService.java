package com.example.demo.Utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SurgePricingService {

	private static final double MAX_SURGE = 3.0;

	private final RedisTemplate<String, String> redisTemplate;

	public SurgePricingService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public double getSurgeMultiplier(String city) {

		if (city == null || city.isBlank()) {
			throw new IllegalArgumentException("City cannot be null or empty");
		}

		try {
			long demand = getActiveRides(city);
			long supply = getAvailableDrivers(city);

			if (supply <= 0) {
				return MAX_SURGE;
			}

			double ratio = (double) demand / supply;

			if (ratio < 1)
				return 1.0;
			if (ratio < 1.5)
				return 1.2;
			if (ratio < 2)
				return 1.5;
			if (ratio < 3)
				return 2.0;
			return MAX_SURGE;

		} catch (Exception ex) {
			throw new SurgePricingException("Error calculating surge pricing for city: " + city, ex);
		}
	}

	private long getActiveRides(String city) {
		try {
			Long count = redisTemplate.opsForSet().size("ride:assignedDriver:" + city);
			return count != null ? count : 0;
		} catch (Exception ex) {
			throw new SurgePricingException("Failed to fetch active rides", ex);
		}
	}

	private long getAvailableDrivers(String city) {
		try {
			Long count = redisTemplate.opsForZSet().size("drivers:geo:available:" + city);
			return count != null ? count : 0;
		} catch (Exception ex) {
			throw new SurgePricingException("Failed to fetch available drivers", ex);
		}
	}
}
