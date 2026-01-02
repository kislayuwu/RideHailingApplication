package com.example.demo.ride.service;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.driver.service.RiderMatchingService;
import com.example.demo.notiification.entities.NotificationMessage;
import com.example.demo.notiification.entities.NotificationType;
import com.example.demo.notiification.service.NotificationService;
import com.example.demo.ride.dto.CreateRideRequest;
import com.example.demo.ride.entity.Ride;
import com.example.demo.ride.entity.RideStatus;
import com.example.demo.ride.exception.RideNotFoundException;
import com.example.demo.ride.repository.RideRepository;

@Service
public class RideService {

	private static final Logger log = LoggerFactory.getLogger(RideService.class);

	private final RideRepository rideRepository;
	private final RiderMatchingService riderMatchingService;
	private final NotificationService notificationService;

	public RideService(RideRepository rideRepository, RiderMatchingService riderMatchingService,
			NotificationService notificationService) {
		this.rideRepository = rideRepository;
		this.riderMatchingService = riderMatchingService;
		this.notificationService = notificationService;
	}

	@Transactional
	public Ride createRide(CreateRideRequest request) {
		try {
			Ride ride = new Ride();
			ride.setId(UUID.randomUUID().toString());
			ride.setCity(request.getCity());
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String riderName = authentication.getName();
			ride.setRiderName(riderName);
			ride.setStatus(RideStatus.REQUESTED);
			ride.setPickupLat(request.getPickupLat());
			ride.setPickupLng(request.getPickupLng());
			ride.setDestinationLat(request.getDestinationLat());
			ride.setDestinationLng(request.getDestinationLng());

			rideRepository.save(ride);
			log.info("Ride created successfully: rideId={}, rider={}", ride.getId(), riderName);

			riderMatchingService.matchDriver(ride);
			log.info("Driver matching initiated for rideId={}", ride.getId());

			return ride;
		} catch (Exception e) {
			log.error("Error creating ride", e);
			throw e;
		}
	}

	@Transactional(readOnly = true)
	public Ride getRide(String rideId) {
		return rideRepository.findById(rideId).orElseThrow(() -> {
			log.warn("Ride not found: rideId={}", rideId);
			return new RideNotFoundException(rideId);
		});
	}

	@Transactional
	public Ride updateStatus(String rideId, RideStatus newStatus) {
		Ride ride = rideRepository.findById(rideId).orElseThrow(() -> {
			log.warn("Ride not found for update: rideId={}", rideId);
			return new RideNotFoundException(rideId);
		});

		log.info("Updating ride status: rideId={}, oldStatus={}, newStatus={}", rideId, ride.getStatus(), newStatus);
		ride.setStatus(newStatus);
		return rideRepository.save(ride);
	}

	public void notifyRiderNoDrivers(String rideId) {
		Ride ride = getRide(rideId);
		log.warn("No drivers available for ride {}", ride.getRiderName());
		notificationService.notify(new NotificationMessage(ride.getRiderName(), NotificationType.NO_DRIVER_AVAILABLE,
				"No driver available", Map.of("rideId", ride.getId())));
	}

}
