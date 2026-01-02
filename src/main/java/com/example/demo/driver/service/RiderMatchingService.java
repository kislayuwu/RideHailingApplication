package com.example.demo.driver.service;

import java.time.Duration;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.driver.entities.Driver;
import com.example.demo.driver.exception.DriverNotFoundException;
import com.example.demo.driver.repository.DriverRepository;
import com.example.demo.notiification.entities.NotificationMessage;
import com.example.demo.notiification.entities.NotificationType;
import com.example.demo.notiification.service.NotificationService;
import com.example.demo.ride.entity.Ride;
import com.example.demo.ride.entity.RideStatus;
import com.example.demo.ride.exception.RideNotFoundException;
import com.example.demo.ride.repository.RideRepository;

@Service
public class RiderMatchingService {

	private static final Logger log = LoggerFactory.getLogger(RiderMatchingService.class);

	private static final int MAX_ATTEMPTS = 5;
	private static final Duration OFFER_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration CANDIDATE_KEY_EXPIRY_DURATION = Duration.ofMinutes(5);

	private final RedisTemplate<String, String> redisTemplate;
	private final NotificationService notificationService;
	private final RideRepository rideRepository;
	private final DriverRepository driverRepository;

	public RiderMatchingService(RedisTemplate<String, String> redisTemplate, RideRepository rideRepository,
			NotificationService notificationService, DriverRepository driverRepository) {
		this.redisTemplate = redisTemplate;
		this.notificationService = notificationService;
		this.rideRepository = rideRepository;
		this.driverRepository = driverRepository;
	}

	@Async("rideMatchingExecutor")
	public void matchDriver(Ride ride) {
		try {
			log.info("Starting driver matching for ride {}", ride.getId());

			String geoKey = "drivers:geo:available:" + ride.getCity();

			GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(geoKey,
					new Circle(new Point(ride.getPickupLng(), ride.getPickupLat()),
							new Distance(5, Metrics.KILOMETERS)),
					RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().sortAscending().limit(MAX_ATTEMPTS));

			if (results == null || results.getContent().isEmpty()) {
				log.warn("No available drivers found for ride {}", ride.getId());
				notifyRiderNoDrivers(ride.getId());
				return;
			}

			String candidateKey = "ride:candidates:" + ride.getId();
			for (GeoResult<RedisGeoCommands.GeoLocation<String>> r : results) {
				redisTemplate.opsForList().rightPush(candidateKey, r.getContent().getName());
			}

			String indexKey = "ride:candidateIndex:" + ride.getId();
			redisTemplate.opsForValue().set(indexKey, "0");

			redisTemplate.expire(candidateKey, CANDIDATE_KEY_EXPIRY_DURATION);
			redisTemplate.expire(indexKey, CANDIDATE_KEY_EXPIRY_DURATION);

			log.info("Stored {} driver candidates for ride {}", results.getContent().size(), ride.getId());

			tryNextDriver(ride.getId());
		} catch (Exception ex) {
			log.error("Fatal error while matching driver for ride {}", ride.getId(), ex);
			updateRideStatus(ride.getId(), RideStatus.NO_DRIVER_AVAILABLE);
			notifyRiderNoDrivers(ride.getId());
			cleanupDriverCandidate(ride.getId());
		}
	}

	public void tryNextDriver(String rideId) {

		String indexKey = "ride:candidateIndex:" + rideId;
		String indexValue = redisTemplate.opsForValue().get(indexKey);

		if (indexValue == null) {
			log.warn("Candidate index missing for ride {}", rideId);
			notifyRiderNoDrivers(rideId);
			return;
		}

		int index = Integer.parseInt(indexValue);

		if (index >= MAX_ATTEMPTS) {
			log.warn("Max attempts reached for ride {}", rideId);
			cleanupDriverCandidate(rideId);
			notifyRiderNoDrivers(rideId);
			updateRideStatus(rideId, RideStatus.NO_DRIVER_AVAILABLE);
			return;
		}

		String driverId = redisTemplate.opsForList().index("ride:candidates:" + rideId, index);
		redisTemplate.opsForValue().increment(indexKey);

		if (driverId == null) {
			log.warn("Driver not found at index {} for ride {}", index, rideId);
			tryNextDriver(rideId);
			return;
		}

		if (!lockDriver(driverId, rideId)) {
			log.info("Driver {} is already locked, trying next for ride {}", driverId, rideId);
			tryNextDriver(rideId);
			return;
		}

		log.info("Sending ride offer to driver {} for ride {}", driverId, rideId);
		sendOfferToDriver(driverId, rideId);

		redisTemplate.opsForValue().set("ride:offer:" + rideId + ":" + driverId, "PENDING", OFFER_TIMEOUT);
	}

	public void handleOfferTimeout(String rideId, String driverId) {

		if (redisTemplate.hasKey("ride:assignedDriver:" + rideId)) {
			log.info("Ride {} already assigned, ignoring timeout for driver {}", rideId, driverId);
			return;
		}

		log.warn("Offer timeout for driver {} on ride {}", driverId, rideId);

		releaseDriverLock(driverId);

		tryNextDriver(rideId);
	}

	private boolean lockDriver(String driverId, String rideId) {
		return Boolean.TRUE
				.equals(redisTemplate.opsForValue().setIfAbsent("driver:lock:" + driverId, rideId, OFFER_TIMEOUT));
	}

	private void releaseDriverLock(String driverId) {
		redisTemplate.delete("driver:lock:" + driverId);
	}

	private void notifyRiderNoDrivers(String rideId) {
		Ride ride = rideRepository.findById(rideId).orElseThrow(() -> {
			log.warn("Ride not found for update: rideId={}", rideId);
			return new RideNotFoundException(rideId);
		});
		log.warn("No drivers available for ride {}", ride.getRiderName());
		notificationService.notify(new NotificationMessage(ride.getRiderName(), NotificationType.NO_DRIVER_AVAILABLE,
				"No driver available", Map.of("rideId", ride.getId())));
	}

	private void cleanupDriverCandidate(String rideId) {
		redisTemplate.delete("ride:candidates:" + rideId);
		redisTemplate.delete("ride:candidateIndex:" + rideId);
		log.debug("Cleaned up candidate data for ride {}", rideId);
	}

	private Ride updateRideStatus(String rideId, RideStatus newStatus) {
		Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RideNotFoundException(rideId));
		log.info("Updating ride status: rideId={}, oldStatus={}, newStatus={}", rideId, ride.getStatus(), newStatus);
		ride.setStatus(newStatus);
		return rideRepository.save(ride);
	}

	private void sendOfferToDriver(String driverId, String rideId) {
		Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new RideNotFoundException(rideId));
		Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new DriverNotFoundException(driverId));

		log.info("Offering ride {} to driver {}", rideId, driverId);
		Map<String, Object> payload = Map.of("rideId", ride.getId(), "riderName", ride.getRiderName(), "pickupLat",
				ride.getPickupLat(), "pickupLng", ride.getPickupLng(), "destinationLat", ride.getDestinationLat(),
				"destinationLng", ride.getDestinationLng(), "city", ride.getCity());

		notificationService.notify(new NotificationMessage(driver.getName(), NotificationType.DRIVER_OFFER,
				"New ride request available", payload));
	}
}
