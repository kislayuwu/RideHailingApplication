package com.example.demo.payment.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Utils.PaymentGatewayClient;
import com.example.demo.notiification.entities.NotificationMessage;
import com.example.demo.notiification.entities.NotificationType;
import com.example.demo.notiification.service.NotificationService;
import com.example.demo.payment.dto.PaymentRequest;
import com.example.demo.payment.entities.Payment;
import com.example.demo.payment.entities.PaymentStatus;
import com.example.demo.payment.exception.PaymentException;
import com.example.demo.payment.repository.PaymentRepository;
import com.example.demo.trip.entities.Trip;
import com.example.demo.trip.entities.TripStatus;
import com.example.demo.trip.exception.TripNotFoundException;
import com.example.demo.trip.repository.TripRepository;

@Service
public class PaymentService {

	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

	private final PaymentRepository paymentRepository;
	private final PaymentGatewayClient paymentGatewayClient;
	private final NotificationService notificationService;
	private final TripRepository tripRepository;

	public PaymentService(PaymentRepository paymentRepository, PaymentGatewayClient paymentGatewayClient,
			NotificationService notificationService, TripRepository tripRepository) {
		this.paymentRepository = paymentRepository;
		this.paymentGatewayClient = paymentGatewayClient;
		this.notificationService = notificationService;
		this.tripRepository = tripRepository;
	}

	@Transactional
	public Payment processPayment(PaymentRequest request) {

		if (request.getTripId() == null || request.getMethod() == null) {
			throw new IllegalArgumentException("TripId or payment method missing");
		}

		log.info("Starting payment for tripId={}", request.getTripId());

		Trip trip = getTripForPayment(request.getTripId());

		if (trip.getStatus() == TripStatus.PAID) {
			log.info("Trip already PAID, returning existing payment");
			return paymentRepository.findByTripId(trip.getId())
					.orElseThrow(() -> new PaymentException("Payment record missing for PAID trip"));
		}

		if (trip.getStatus() != TripStatus.ENDED && trip.getStatus() != TripStatus.PAYMENT_FAILED) {
			throw new IllegalStateException("Trip not ready for payment");
		}

		Payment payment = paymentRepository.findByTripId(trip.getId()).orElseGet(() -> createNewPayment(trip));

		if (payment.getStatus() == PaymentStatus.SUCCESS) {
			return payment;
		}

		boolean success = chargePSP(payment, request);

		if (success) {
			handlePaymentSuccess(trip, payment);
		} else {
			handlePaymentFailure(trip, payment);
		}

		tripRepository.save(trip);
		return paymentRepository.save(payment);
	}

	private Trip getTripForPayment(String tripId) {
		Trip trip = tripRepository.findByIdForPayment(tripId).orElseThrow(() -> new TripNotFoundException(tripId));
		return trip;
	}

	private Payment createNewPayment(Trip trip) {
		Payment payment = new Payment();
		payment.setId(UUID.randomUUID().toString());
		payment.setTripId(trip.getId());
		payment.setAmount(trip.getFare());
		payment.setStatus(PaymentStatus.INITIATED);
		payment.setCreatedAt(Instant.now());
		return paymentRepository.save(payment);
	}

	private boolean chargePSP(Payment payment, PaymentRequest request) {
		try {
			log.info("Charging amount={} via method={}", payment.getAmount(), request.getMethod());

			boolean success = paymentGatewayClient.charge(request.getMethod(), payment.getAmount());

			payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
			payment.setPspReference(UUID.randomUUID().toString());

			return success;

		} catch (Exception ex) {
			log.error("Payment gateway error for paymentId={}", payment.getId(), ex);
			payment.setStatus(PaymentStatus.FAILED);
			return false;
		}
	}

	private void handlePaymentSuccess(Trip trip, Payment payment) {

		log.info("Payment successful for trip {}", trip.getId());
		trip.setStatus(TripStatus.PAID);

		Map<String, Object> riderPayload = Map.of("amount", payment.getAmount(), "driverName", trip.getDriverName(),
				"pspReference", payment.getPspReference());

		Map<String, Object> driverPayload = Map.of("amount", payment.getAmount(), "riderName", trip.getRiderName(),
				"pspReference", payment.getPspReference());

		notificationService.notify(new NotificationMessage(trip.getRiderName(), NotificationType.PAYMENT_SUCCESS,
				"Payment successful", riderPayload));

		notificationService.notify(new NotificationMessage(trip.getDriverName(), NotificationType.PAYMENT_SUCCESS,
				"Payment received", driverPayload));
	}

	private void handlePaymentFailure(Trip trip, Payment payment) {

		log.warn("Payment failed for trip {}", trip.getId());
		trip.setStatus(TripStatus.PAYMENT_FAILED);

		notificationService.notify(new NotificationMessage(trip.getRiderName(), NotificationType.PAYMENT_FAILED,
				"Payment failed. Please retry.",
				Map.of("amount", payment.getAmount(), "pspReference", payment.getPspReference())));
	}

}
