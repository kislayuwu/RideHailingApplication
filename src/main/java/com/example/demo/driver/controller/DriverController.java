package com.example.demo.driver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.driver.dto.AcceptRideRequest;
import com.example.demo.driver.dto.DriverDetailsRequest;
import com.example.demo.driver.entities.Driver;
import com.example.demo.driver.service.DriverService;
import com.example.demo.trip.entities.Trip;

@RestController
@RequestMapping("/v1/drivers")
public class DriverController {

	private static final Logger log = LoggerFactory.getLogger(DriverController.class);

	private final DriverService driverService;

	public DriverController(DriverService driverService) {
		this.driverService = driverService;
	}

	@PostMapping
	public ResponseEntity<Driver> createDriver(@RequestBody Driver driver) {
		log.info("Create driver request received");
		return ResponseEntity.ok(driverService.createDriver(driver));
	}
	
	@PostMapping("/details")
	public ResponseEntity<Driver> getDriver(@RequestBody DriverDetailsRequest request) {
	    log.info("Get driver request received for {}", request.getDriverName());
	    return ResponseEntity.ok(
	        driverService.getDriverByName(request.getDriverName())
	    );
	}

	@PostMapping("/{driverId}/accept")
	public ResponseEntity<Trip> acceptRide(@PathVariable String driverId, @RequestBody AcceptRideRequest request) {

		log.info("Accept ride request: driverId={}, rideId={}", driverId, request.getRideId());

		Trip trip = driverService.acceptRide(request.getRideId(), driverId);
		return ResponseEntity.ok(trip);
	}

	@PostMapping("/{driverId}/reject")
	public ResponseEntity<Void> rejectRide(@PathVariable String driverId, @RequestBody AcceptRideRequest request) {
		log.info("Reject ride request: driverId={}, rideId={}", driverId, request.getRideId());
		driverService.rejectRide(request.getRideId(), driverId);
		return ResponseEntity.ok().build();
	}

}
