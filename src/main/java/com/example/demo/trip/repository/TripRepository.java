package com.example.demo.trip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.trip.entities.Trip;

import jakarta.persistence.LockModeType;

public interface TripRepository extends JpaRepository<Trip, String> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.id = :id")
    Optional<Trip> findByIdForPayment(@Param("id") String id);
}
