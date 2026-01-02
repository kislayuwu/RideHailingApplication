package com.example.demo.ride.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.ride.entity.Ride;

public interface RideRepository extends JpaRepository<Ride, String> {

}
