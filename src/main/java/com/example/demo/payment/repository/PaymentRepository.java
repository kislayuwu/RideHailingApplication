package com.example.demo.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.example.demo.payment.entities.Payment;

import jakarta.persistence.LockModeType;

public interface PaymentRepository extends JpaRepository<Payment, String> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Payment> findTopByTripIdOrderByCreatedAtDesc(String tripId);
}
