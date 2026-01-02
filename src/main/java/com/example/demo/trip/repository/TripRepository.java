package com.example.demo.trip.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.trip.entities.Trip;

public interface TripRepository extends JpaRepository<Trip, String> {

}
