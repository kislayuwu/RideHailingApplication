package com.example.demo.Utils;

import org.springframework.stereotype.Component;

import com.example.demo.payment.dto.Paymentmethod;

@Component
public class PaymentGatewayClient {

	public boolean charge(Paymentmethod paymentMethod, double amount) {
		// Simulate PSP latency
		try {
			Thread.sleep(200);
		} catch (InterruptedException ignored) {
		}

		if (paymentMethod == Paymentmethod.CASH) {
			return true;
		}

		// 90% success simulation
		return Math.random() < 0.9;
	}
}
