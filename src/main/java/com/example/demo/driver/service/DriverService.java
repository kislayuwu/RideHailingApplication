package com.example.demo.driver.service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.driver.entities.Driver;
import com.example.demo.driver.exception.DriverNotFoundException;
import com.example.demo.driver.exception.RideOfferExpiredException;
import com.example.demo.driver.repository.DriverRepository;
import com.example.demo.notiification.entities.NotificationMessage;
import com.example.demo.notiification.entities.NotificationType;
import com.example.demo.notiification.service.NotificationService;
import com.example.demo.ride.entity.Ride;
import com.example.demo.ride.entity.RideStatus;
import com.example.demo.ride.service.RideService;
import com.example.demo.trip.entities.Trip;
import com.example.demo.trip.service.TripService;

@Service
public class DriverService {

	private static final Logger log = LoggerFactory.getLogger(DriverService.class);

	private final DriverRepository driverRepository;
	private final RideService rideService;
	private final TripService tripService;
	private final RedisTemplate<String, String> redisTemplate;
	private final RiderMatchingService riderMatchingService;
	private final NotificationService notificationService;

	public DriverService(DriverRepository driverRepository, RideService rideService, TripService tripService,
			RedisTemplate<String, String> redisTemplate, RiderMatchingService riderMatchingService,
			NotificationService notificationService) {
		this.driverRepository = driverRepository;
		this.rideService = rideService;
		this.tripService = tripService;
		this.redisTemplate = redisTemplate;
		this.riderMatchingService = riderMatchingService;
		this.notificationService = notificationService;
	}

	public Driver createDriver(Driver driver) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		driver.setId(UUID.randomUUID().toString());
		driver.setName(auth.getName());
		driver.setAssigned(false);
		log.info("Creating driver: {}", driver.getName());
		driverRepository.save(driver);
		redisTemplate.opsForGeo().add("drivers:geo:available:" + driver.getCity(),
				new Point(driver.getLng(), driver.getLat()), driver.getId());
		return driver;
	}

	public Driver getDriverByName(String driverName) {
		return driverRepository.findByName(driverName).orElseThrow(() -> new DriverNotFoundException(driverName));
	}

	@Transactional
	public Trip acceptRide(String rideId, String driverId) {

		log.info("Driver {} attempting to accept ride {}", driverId, rideId);

		String offerKey = "ride:offer:" + rideId + ":" + driverId;
		String assignKey = "ride:assignedDriver:" + rideId;

		if (!Boolean.TRUE.equals(redisTemplate.hasKey(offerKey))) {
			log.warn("Offer expired for driver={}, ride={}", driverId, rideId);
			throw new RideOfferExpiredException("Ride offer expired or invalid");
		}

		Boolean assigned = redisTemplate.opsForValue()
	            .setIfAbsent(assignKey, driverId, Duration.ofSeconds(30));

		if (!Boolean.TRUE.equals(assigned)) {
			log.warn("Ride {} already assigned", rideId);
			throw new IllegalStateException("Ride already assigned");
		}

		Ride ride = rideService.updateStatus(rideId, RideStatus.DRIVER_ASSIGNED);

		Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new DriverNotFoundException(driverId));

		markDriverAssigned(driver);

		Trip trip = tripService.createTrip(ride, driver);

		notifyRiderDriverAssigned(trip);
		cleanupDriverCandidate(rideId);

		log.info("Trip {} created successfully for ride {}", trip.getId(), rideId);
		return trip;
	}

	public void rejectRide(String rideId, String driverId) {
		log.info("Driver {} rejected ride {}", driverId, rideId);
		redisTemplate.delete("ride:offer:" + rideId + ":" + driverId);
		redisTemplate.delete("driver:lock:" + driverId);
		riderMatchingService.tryNextDriver(rideId);
	}

	private void markDriverAssigned(Driver driver) {
		redisTemplate.opsForGeo().remove("drivers:geo:available:" + driver.getCity(), driver.getId());
		driver.setAssigned(true);
		driverRepository.save(driver);
	}

	private void notifyRiderDriverAssigned(Trip trip) {
		log.info("Notifying rider: driver {} assigned to trip {}", trip.getDriverName(), trip.getId());

		Map<String, Object> payload = Map.of("id", trip.getId(), "riderName", trip.getRiderName(), "driverName",
				trip.getDriverName(), "pickupLat", trip.getPickupLat(), "pickupLng", trip.getPickupLng(),
				"destinationLat", trip.getDestinationLat(), "destinationLng", trip.getDestinationLng(), "startTime",
				trip.getStartTime());

		notificationService.notify(new NotificationMessage(trip.getRiderName(), NotificationType.DRIVER_ASSIGNED,
				"Driver Assigned", payload));
	}

	private void cleanupDriverCandidate(String rideId) {
		redisTemplate.delete("ride:candidates:" + rideId);
		redisTemplate.delete("ride:candidateIndex:" + rideId);
	}

}
