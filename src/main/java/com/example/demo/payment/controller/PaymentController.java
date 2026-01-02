package com.example.demo.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.payment.dto.PaymentRequest;
import com.example.demo.payment.entities.Payment;
import com.example.demo.payment.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public ResponseEntity<Payment> pay(@RequestBody PaymentRequest request) {
		Payment payment = paymentService.processPayment(request);
		return ResponseEntity.ok(payment);
	}

}
