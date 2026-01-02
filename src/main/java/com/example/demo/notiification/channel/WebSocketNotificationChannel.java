package com.example.demo.notiification.channel;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.demo.notiification.entities.NotificationMessage;
import com.example.demo.notiification.entities.NotificationType;

@Component
public class WebSocketNotificationChannel implements NotificationChannel {

	private final SimpMessagingTemplate messagingTemplate;

	public WebSocketNotificationChannel(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@Override
	public boolean supports(NotificationType type) {
		return switch (type) {
		case DRIVER_OFFER, DRIVER_ASSIGNED, NO_DRIVER_AVAILABLE, TRIP_COMPLETED, PAYMENT_SUCCESS, PAYMENT_FAILED ->
			true;
		default -> false;
		};
	}

	@Override
	public void send(NotificationMessage message) {
		messagingTemplate.convertAndSend("/topic/notifications/" + message.getUserId(), message);
	}
}
