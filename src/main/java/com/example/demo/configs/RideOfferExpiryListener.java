package com.example.demo.configs;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.example.demo.driver.service.RiderMatchingService;

@Component
public class RideOfferExpiryListener extends KeyExpirationEventMessageListener {

    private final RiderMatchingService riderMatchingService;

    public RideOfferExpiryListener(
            RedisMessageListenerContainer listenerContainer,
            RiderMatchingService riderMatchingService) {
        super(listenerContainer);
        this.riderMatchingService = riderMatchingService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (!expiredKey.startsWith("ride:offer:")) {
            return;
        }

        String[] parts = expiredKey.split(":");
        String rideId = parts[2];
        String driverId = parts[3];

        // Release driver lock
        riderMatchingService.handleOfferTimeout(rideId, driverId);
    }
}
