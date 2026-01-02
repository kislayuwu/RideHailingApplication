package com.example.demo.trip.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Utils.FareCalculator;
import com.example.demo.Utils.PaymentGatewayClient;
import com.example.demo.driver.entities.Driver;
import com.example.demo.driver.exception.DriverNotFoundException;
import com.example.demo.driver.repository.DriverRepository;
import com.example.demo.notiification.entities.NotificationMessage;
import com.example.demo.notiification.entities.NotificationType;
import com.example.demo.notiification.service.NotificationService;
import com.example.demo.payment.repository.PaymentRepository;
import com.example.demo.ride.entity.Ride;
import com.example.demo.trip.entities.Trip;
import com.example.demo.trip.entities.TripStatus;
import com.example.demo.trip.exception.InvalidTripStateException;
import com.example.demo.trip.exception.TripNotFoundException;
import com.example.demo.trip.repository.TripRepository;

@Service
public class TripService {

	private static final Logger log = LoggerFactory.getLogger(TripService.class);
	private static final double EARTH_RADIUS_KM = 6371;

	private final TripRepository tripRepository;
	private final FareCalculator fareCalculator;
	private final DriverRepository driverRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final NotificationService notificationService;

	public TripService(TripRepository tripRepository, RedisTemplate<String, String> redisTemplate,
			PaymentRepository paymentRepository, FareCalculator fareCalculator,
			PaymentGatewayClient paymentGatewayClient, DriverRepository driverRepository,
			NotificationService notificationService) {

		this.tripRepository = tripRepository;
		this.redisTemplate = redisTemplate;
		this.fareCalculator = fareCalculator;
		this.driverRepository = driverRepository;
		this.notificationService = notificationService;
	}

	@Transactional
	public Trip createTrip(Ride ride, Driver driver) {

		log.info("Creating trip for rider={} driver={}", ride.getRiderName(), driver.getId());

		Trip trip = new Trip();
		trip.setId(UUID.randomUUID().toString());
		trip.setDriverName(driver.getName());
		trip.setRiderName(ride.getRiderName());
		trip.setStatus(TripStatus.STARTED);
		trip.setStartTime(Instant.now());
		trip.setCity(ride.getCity());
		trip.setPickupLat(ride.getPickupLat());
		trip.setPickupLng(ride.getPickupLng());
		trip.setDestinationLat(ride.getDestinationLat());
		trip.setDestinationLng(ride.getDestinationLng());

		return tripRepository.save(trip);
	}

	@Transactional
	public Trip endTrip(String tripId) {

		log.info("Ending trip {} ", tripId);

		Trip trip = tripRepository.findById(tripId).orElseThrow(() -> {
			log.warn("Trip {} not found", tripId);
			return new TripNotFoundException(tripId);
		});

		if (trip.getStatus() != TripStatus.STARTED) {
			log.warn("Trip {} invalid state: {}", tripId, trip.getStatus());
			throw new InvalidTripStateException("Trip not in STARTED state");
		}

		trip.setEndTime(Instant.now());
		trip.setStatus(TripStatus.ENDED);double distance = calculateDistance(trip);
		trip.setFare(fareCalculator.calculate(distance, trip.getCity()));
		tripRepository.save(trip);

		releaseDriver(trip);
		notifyRiderTripCompleted(trip);
		return tripRepository.save(trip);
	}

	private void releaseDriver(Trip trip) {

		String driverName = trip.getDriverName();
		log.info("Releasing driver {}", driverName);

		Driver driver = driverRepository.findByName(driverName)
				.orElseThrow(() -> new DriverNotFoundException(driverName));

		driver.setLat(trip.getDestinationLat());
		driver.setLng(trip.getDestinationLng());
		driver.setAssigned(false);
		driverRepository.save(driver);

		String city = driver.getCity();
		Point destination = new Point(trip.getDestinationLng(), trip.getDestinationLat());
		redisTemplate.opsForGeo().add("drivers:geo:available:" + city, destination, driver.getId());

		log.info("Driver {} released at destination location [{} , {}]", driver.getId(), trip.getDestinationLat(),
				trip.getDestinationLng());
	}

	private double calculateDistance(Trip trip) {

		double lat1 = trip.getPickupLat();
		double lng1 = trip.getPickupLng();
		double lat2 = trip.getDestinationLat();
		double lng2 = trip.getDestinationLng();

		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS_KM * c;
	}

	private void notifyRiderTripCompleted(Trip trip) {
		log.warn("Notify Rider {} of trip completion", trip.getRiderName());
		Map<String, Object> payload = Map.of("id", trip.getId(), "riderName", trip.getRiderName(), "driverName",
				trip.getDriverName(), "pickupLat", trip.getPickupLat(), "pickupLng", trip.getPickupLng(),
				"destinationLat", trip.getDestinationLat(), "destinationLng", trip.getDestinationLng(), "startTime",
				trip.getStartTime(), "fare", trip.getFare());
		notificationService.notify(new NotificationMessage(trip.getRiderName(), NotificationType.TRIP_COMPLETED,
				"Trip Completed", payload));
	}
}
