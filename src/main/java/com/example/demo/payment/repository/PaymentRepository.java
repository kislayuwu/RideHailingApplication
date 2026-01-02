package com.example.demo.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.payment.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, String> {
	Optional<Payment> findTopByTripIdOrderByCreatedAtDesc(String tripId);

	Optional<Payment> findByTripId(String id);
}
