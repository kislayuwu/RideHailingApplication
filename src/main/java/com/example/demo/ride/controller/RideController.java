package com.example.demo.ride.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.ride.dto.CreateRideRequest;
import com.example.demo.ride.entity.Ride;
import com.example.demo.ride.entity.RideStatus;
import com.example.demo.ride.service.RideService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/rides")
public class RideController {

	private static final Logger log = LoggerFactory.getLogger(RideService.class);
	private final RideService rideService;

	public RideController(RideService rideService) {
		this.rideService = rideService;
	}

	@PostMapping
	public ResponseEntity<Ride> createRide(@Valid @RequestBody CreateRideRequest request) {
		log.info("CreateRideRequest received: {}", request);
		Ride ride = rideService.createRide(request);
		return ResponseEntity.ok(ride);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Ride> getRide(@PathVariable String id) {
		log.info("Get ride request received: rideId={}", id);
		return ResponseEntity.ok(rideService.getRide(id));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<Ride> updateStatus(@PathVariable String id, @RequestParam RideStatus status) {
		log.info("Update ride status request: rideId={}, newStatus={}", id, status);
		return ResponseEntity.ok(rideService.updateStatus(id, status));
	}
}