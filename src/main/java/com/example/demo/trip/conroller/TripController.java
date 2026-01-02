package com.example.demo.trip.conroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.trip.entities.Trip;
import com.example.demo.trip.service.TripService;

@RestController
@RequestMapping("/v1/trips")
public class TripController {

	private static final Logger log = LoggerFactory.getLogger(TripController.class);

	private final TripService tripService;

	public TripController(TripService tripService) {
		this.tripService = tripService;
	}

	@PostMapping("/{id}/end")
	public ResponseEntity<Trip> endTrip(@PathVariable String id) {

		log.info("End trip request received: tripId={}", id);

		Trip trip = tripService.endTrip(id);

		log.info("Trip ended successfully: tripId={}, status={}", id, trip.getStatus());

		return ResponseEntity.ok(trip);
	}
}
